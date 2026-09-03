package br.com.walletzen.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

/**
 * Identidade de quem chamou, extraída do JWT.
 *
 * <p>O {@code wz-user} usa o {@code wz_user.id} como {@code username} do Keycloak, então o
 * claim {@code preferred_username} do token carrega esse id. Usuários seed do realm
 * ({@code alice}, {@code admin}) têm {@code preferred_username} não-UUID → {@code wzUserId}
 * fica {@code null} (não são donos de nada; {@code admin} passa pela role).
 *
 * @param wzUserId id do usuário no wz-user, ou {@code null}.
 */
public record Caller(UUID wzUserId, boolean admin) {

    public static Caller from(JwtAuthenticationToken token) {
        if (token == null) {
            return new Caller(null, false);
        }
        UUID id = null;
        String claim = token.getToken().getClaimAsString("preferred_username");
        if (claim != null && !claim.isBlank()) {
            try {
                id = UUID.fromString(claim);
            } catch (IllegalArgumentException ignored) {
                // não é um wz_user.id (usuário seed) -> tratado como sem dono
            }
        }
        boolean admin = token.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return new Caller(id, admin);
    }

    public boolean owns(UUID resourceOwnerId) {
        return admin || resourceOwnerId.equals(wzUserId);
    }
}
