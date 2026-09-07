package br.com.walletzen.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/** {@code data} do evento {@code UserDeleted} publicado por {@code wz-user}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDeletedData(UUID userId) {
}
