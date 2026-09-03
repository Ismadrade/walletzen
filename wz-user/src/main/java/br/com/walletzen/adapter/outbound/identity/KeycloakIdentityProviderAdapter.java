package br.com.walletzen.adapter.outbound.identity;

import br.com.walletzen.core.exception.IdentityProviderException;
import br.com.walletzen.core.port.output.IdentityProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakIdentityProviderAdapter implements IdentityProviderPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakIdentityProviderAdapter.class);

    private final KeycloakAdminTokenProvider tokenProvider;
    private final RestClient http;

    public KeycloakIdentityProviderAdapter(KeycloakAdminProperties props, KeycloakAdminTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.http = RestClient.builder()
                .baseUrl(props.baseUrl() + "/admin/realms/" + props.realm())
                .build();
    }

    @Override
    public String createUser(String wzUserId, String email, String firstName, String lastName, String rawPassword) {
        try {
            URI location = http.post()
                    .uri("/users")
                    .headers(h -> h.setBearerAuth(tokenProvider.token()))
                    .body(Map.of(
                            // username = wz_user.id (imutável) -> vira o claim `preferred_username`
                            // no token, usado pelo wz-financial para autorização por dono.
                            "username", wzUserId,
                            "email", email,
                            "firstName", firstName,
                            "lastName", lastName,
                            "enabled", true,
                            "emailVerified", true,
                            "credentials", List.of(Map.of(
                                    "type", "password",
                                    "value", rawPassword,
                                    "temporary", false))))
                    .retrieve()
                    .toBodilessEntity()
                    .getHeaders()
                    .getLocation();

            if (location == null) {
                throw new IdentityProviderException("Keycloak não retornou o Location do usuário criado");
            }
            String id = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
            try {
                assignRealmRole(id, "USER");
            } catch (RestClientException roleFailure) {
                // usuário já foi criado — não deixa órfão
                deleteUser(id);
                throw new IdentityProviderException(
                        "Falha ao atribuir a role USER no Keycloak: " + roleFailure.getMessage(), roleFailure);
            }
            return id;
        } catch (RestClientException e) {
            throw new IdentityProviderException("Falha ao criar usuário no Keycloak: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateUser(String identityId, String email, String firstName, String lastName) {
        put(identityId, Map.of("email", email, "firstName", firstName, "lastName", lastName),
                "atualizar usuário");
    }

    @Override
    public void enableUser(String identityId) {
        put(identityId, Map.of("enabled", true), "reabilitar usuário");
    }

    @Override
    public void resetPassword(String identityId, String rawPassword) {
        try {
            http.put()
                    .uri("/users/{id}/reset-password", identityId)
                    .headers(h -> h.setBearerAuth(tokenProvider.token()))
                    .body(Map.of("type", "password", "value", rawPassword, "temporary", false))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new IdentityProviderException("Falha ao redefinir a senha no Keycloak: " + e.getMessage(), e);
        }
    }

    @Override
    public void disableUser(String identityId) {
        put(identityId, Map.of("enabled", false), "desabilitar usuário");
    }

    @Override
    public void deleteUser(String identityId) {
        try {
            http.delete()
                    .uri("/users/{id}", identityId)
                    .headers(h -> h.setBearerAuth(tokenProvider.token()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // compensação: loga e segue — o registro em wz_user já foi revertido pela exceção original
            log.error("Falha ao compensar (deletar) usuário {} no Keycloak", identityId, e);
        }
    }

    private void put(String identityId, Map<String, ?> body, String action) {
        try {
            http.put()
                    .uri("/users/{id}", identityId)
                    .headers(h -> h.setBearerAuth(tokenProvider.token()))
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new IdentityProviderException("Falha ao " + action + " no Keycloak: " + e.getMessage(), e);
        }
    }

    private void assignRealmRole(String identityId, String roleName) {
        Map<String, Object> role = http.get()
                .uri("/roles/{name}", roleName)
                .headers(h -> h.setBearerAuth(tokenProvider.token()))
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});

        http.post()
                .uri("/users/{id}/role-mappings/realm", identityId)
                .headers(h -> h.setBearerAuth(tokenProvider.token()))
                .body(List.of(Map.of("id", role.get("id"), "name", role.get("name"))))
                .retrieve()
                .toBodilessEntity();
    }
}
