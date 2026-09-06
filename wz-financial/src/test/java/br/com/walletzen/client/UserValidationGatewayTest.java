package br.com.walletzen.client;

import br.com.walletzen.exception.UnknownUserException;
import br.com.walletzen.exception.UserServiceUnavailableException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Cobre a lógica do gateway sem AOP: mapeamento de erro na chamada e os fallbacks.
 * O comportamento de "circuito abre / refecha" (Resilience4j em runtime) é verificado
 * manualmente no Docker — ver plano 07 / README — e migra para teste de integração na Fase 7.
 */
@ExtendWith(MockitoExtension.class)
class UserValidationGatewayTest {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private UserValidationGateway gateway;

    private final UUID userId = UUID.randomUUID();

    private static FeignException feign(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://wz-user/users/x",
                Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return FeignException.errorStatus("UserClient#getById",
                feign.Response.builder().status(status).request(request).build());
    }

    @Test
    @DisplayName("2xx de wz-user -> não lança")
    void activeUserPasses() {
        when(userClient.getById(userId)).thenReturn(new UserSummary(userId, "a@walletzen.com.br"));

        assertDoesNotThrow(() -> gateway.assertActiveUser(userId));
    }

    @Test
    @DisplayName("404 de wz-user -> UnknownUserException (inexistente ou inativo)")
    void notFoundBecomesUnknownUser() {
        doThrow(feign(404)).when(userClient).getById(userId);

        UnknownUserException ex = assertThrows(UnknownUserException.class,
                () -> gateway.assertActiveUser(userId));
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains(userId.toString()));
    }

    @Test
    @DisplayName("fallback de indisponibilidade envolve a causa em UserServiceUnavailableException")
    void unavailableFallbackWraps() {
        Throwable cause = feign(503);

        UserServiceUnavailableException ex = assertThrows(UserServiceUnavailableException.class,
                () -> gateway.unavailable(userId, cause));
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("fallback específico de UnknownUserException repassa o 422 sem mascarar")
    void unknownUserFallbackRethrows() {
        UnknownUserException original = new UnknownUserException(userId);

        UnknownUserException ex = assertThrows(UnknownUserException.class,
                () -> gateway.unavailable(userId, original));
        assertSame(original, ex);
    }
}
