package br.com.walletzen.adapter.outbound.outbox;

import java.time.Instant;
import java.util.UUID;

/**
 * Envelope versionado dos eventos de domínio do WalletZen (v1). {@code wz-financial}
 * tem a cópia consumidora deste record.
 */
public record EventEnvelope(
        UUID eventId,
        String eventType,
        int version,
        Instant occurredAt,
        String aggregateType,
        String aggregateId,
        Object data) {
}
