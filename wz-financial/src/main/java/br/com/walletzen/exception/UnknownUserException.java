package br.com.walletzen.exception;

import java.util.UUID;

/**
 * O {@code userId} dono do lançamento não existe em {@code wz-user} — ou existe mas
 * está inativo (soft-deleted), caso em que {@code wz-user} também devolve 404.
 * Mapeado para HTTP 422: o body do request está bem-formado, mas referencia um usuário inválido.
 */
public class UnknownUserException extends RuntimeException {
    public UnknownUserException(UUID userId) {
        super("user " + userId + " not found or inactive");
    }
}
