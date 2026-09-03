package br.com.walletzen.core.exception;

/** Falha ao falar com o Identity Provider (Keycloak Admin API). */
public class IdentityProviderException extends RuntimeException {

    public IdentityProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentityProviderException(String message) {
        super(message);
    }
}
