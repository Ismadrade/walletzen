package br.com.walletzen.exception;

import java.util.UUID;

/**
 * Não foi possível validar o dono do lançamento porque {@code wz-user} está indisponível
 * (timeout, erro 5xx, IO, ou circuito aberto). Fail-closed: o lançamento não é aceito.
 * Mapeado para HTTP 503.
 */
public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(UUID userId, Throwable cause) {
        super("could not validate user " + userId + ": user service unavailable", cause);
    }
}
