package br.com.walletzen.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Cliente HTTP para {@code wz-user}, resolvido pelo id no Eureka (spring-cloud-loadbalancer).
 * {@code path = "/users"} casa com o {@code server.servlet.context-path} de {@code wz-user}.
 *
 * <p>Os timeouts vêm de {@code spring.cloud.openfeign.client.config.wz-user.*} (config-server).
 * A resiliência (circuit breaker + retry + fallback) fica em {@link UserValidationGateway},
 * não aqui — anotações do Resilience4j não pegam num método de interface Feign.
 */
@FeignClient(name = "wz-user", path = "/users", configuration = UserClientConfig.class)
public interface UserClient {

    @GetMapping("/{id}")
    UserSummary getById(@PathVariable("id") UUID id);
}
