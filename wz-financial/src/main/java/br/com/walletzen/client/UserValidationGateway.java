package br.com.walletzen.client;

import br.com.walletzen.exception.UnknownUserException;
import br.com.walletzen.exception.UserServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fronteira resiliente para a validação síncrona do dono do lançamento em {@code wz-user}.
 *
 * <ul>
 *   <li>{@link Retry}: reexecuta em erro transitório ({@code FeignException} 5xx / IO);
 *       {@link UnknownUserException} está em {@code ignore-exceptions} — 404 não se reintenta.</li>
 *   <li>{@link CircuitBreaker}: após taxa de falha no limite, abre e as chamadas seguintes
 *       caem direto no fallback (sem esperar timeout). {@link UnknownUserException} está em
 *       {@code ignore-exceptions} — não conta como falha do circuito.</li>
 *   <li>Fallback fail-closed: {@code wz-user} indisponível ⇒ {@link UserServiceUnavailableException}
 *       (HTTP 503). Nunca aceitamos o lançamento sem validar.</li>
 * </ul>
 *
 * O limite de tempo por chamada vem do {@code connect/read-timeout} do Feign
 * ({@code spring.cloud.openfeign.client.config.wz-user.*}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidationGateway {

    static final String INSTANCE = "wz-user";

    private final UserClient userClient;

    @Retry(name = INSTANCE)
    @CircuitBreaker(name = INSTANCE, fallbackMethod = "unavailable")
    public void assertActiveUser(UUID userId) {
        try {
            userClient.getById(userId); // 2xx => usuário existe e está ativo
        } catch (FeignException.NotFound e) {
            // wz-user faz GET com WHERE record_status = true => 404 cobre inexistente E inativo
            throw new UnknownUserException(userId);
        }
    }

    /**
     * Usuário inválido: propaga como está (vira 422); não é indisponibilidade.
     * Package-private (não {@code private}) para o Resilience4j resolver o fallback e para teste direto.
     */
    void unavailable(UUID userId, UnknownUserException e) {
        throw e;
    }

    /** Circuito aberto, timeout, 5xx ou IO: fail-closed. */
    void unavailable(UUID userId, Throwable t) {
        log.warn("Validação do usuário {} falhou ({}): recusando o lançamento (fail-closed)",
                userId, t.toString());
        throw new UserServiceUnavailableException(userId, t);
    }
}
