package br.com.walletzen.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * Envelope versionado que embrulha todo evento de domínio do WalletZen (v1).
 * {@code data} fica genérico ({@code Object}) — cada consumidor faz
 * {@code objectMapper.convertValue(data, XxxData.class)} conforme o {@code eventType}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope(
        UUID eventId,
        String eventType,
        int version,
        Instant occurredAt,
        String aggregateType,
        String aggregateId,
        Object data) {
}
