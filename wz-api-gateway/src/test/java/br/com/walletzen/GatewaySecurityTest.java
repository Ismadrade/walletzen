package br.com.walletzen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class GatewaySecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("/actuator/health é aberto")
    void healthIsOpen() {
        webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("rota protegida sem token -> 401")
    void protectedRouteWithoutToken() {
        webTestClient.get().uri("/users/anything").exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("preflight CORS da SPA passa sem token e traz Allow-Origin")
    void corsPreflightIsAllowed() {
        webTestClient.options().uri("/users/anything")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:5173");
    }
}
