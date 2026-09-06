package br.com.walletzen.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Projeção mínima do {@code UserResponse} de {@code wz-user}. Só precisamos saber que a
 * chamada devolveu 2xx (usuário existe e está ativo); os demais campos são ignorados.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserSummary(UUID id, String email) {
}
