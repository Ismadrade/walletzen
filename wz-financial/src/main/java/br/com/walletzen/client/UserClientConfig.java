package br.com.walletzen.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Config do {@link UserClient}. Não leva {@code @Configuration} de propósito: o Feign a
 * instancia por client, sem entrar no component scan global.
 *
 * <p>{@code GET /users/{id}} exige autenticação em {@code wz-user}, então repassamos o
 * Bearer do chamador (TokenRelay serviço→serviço). Fora de um contexto de request
 * (ex.: consumer Kafka) não há token — a chamada seguiria sem header e falharia 401,
 * que o {@link UserValidationGateway} trata como indisponibilidade. Hoje nenhum caminho
 * fora de request chama o {@link UserClient}.
 */
public class UserClientConfig {

    @Bean
    public RequestInterceptor bearerTokenRelayInterceptor() {
        return template -> {
            if (SecurityContextHolder.getContext().getAuthentication()
                    instanceof JwtAuthenticationToken jwtAuth) {
                template.header("Authorization", "Bearer " + jwtAuth.getToken().getTokenValue());
            }
        };
    }
}
