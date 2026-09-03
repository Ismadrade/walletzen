package br.com.walletzen.adapter.outbound.identity;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Config do acesso à Admin API do Keycloak (prefixo {@code keycloak.admin}).
 */
@ConfigurationProperties(prefix = "keycloak.admin")
public record KeycloakAdminProperties(
        String baseUrl,
        String realm,
        String tokenUri,
        String clientId,
        String clientSecret
) {
}
