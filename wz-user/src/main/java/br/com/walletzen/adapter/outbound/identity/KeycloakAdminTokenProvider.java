package br.com.walletzen.adapter.outbound.identity;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/**
 * Token de admin do Keycloak via {@code client_credentials} no client
 * {@code wz-user-service}. Cacheia até {@code expires_in - 30s}.
 */
@Component
class KeycloakAdminTokenProvider {

    private final KeycloakAdminProperties props;
    private final RestClient http = RestClient.create();

    private String cachedToken;
    private Instant expiresAt = Instant.EPOCH;

    KeycloakAdminTokenProvider(KeycloakAdminProperties props) {
        this.props = props;
    }

    synchronized String token() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt)) {
            return cachedToken;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", props.clientId());
        form.add("client_secret", props.clientSecret());

        TokenResponse res = http.post()
                .uri(props.tokenUri())
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (res == null || res.accessToken() == null) {
            throw new IllegalStateException("Keycloak não retornou access_token para o service account");
        }
        cachedToken = res.accessToken();
        expiresAt = Instant.now().plusSeconds(Math.max(res.expiresIn() - 30, 5));
        return cachedToken;
    }

    private record TokenResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("access_token") String accessToken,
            @com.fasterxml.jackson.annotation.JsonProperty("expires_in") long expiresIn) {
    }
}
