package br.com.walletzen.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita os clientes Feign de {@code br.com.walletzen.client}. Isolado numa {@code @Configuration}
 * própria (em vez de na classe {@code @SpringBootApplication}) para que os testes de slice
 * ({@code @WebMvcTest}) não registrem os proxies Feign sem a auto-config correspondente.
 */
@Configuration
@EnableFeignClients(basePackages = "br.com.walletzen.client")
public class FeignConfig {
}
