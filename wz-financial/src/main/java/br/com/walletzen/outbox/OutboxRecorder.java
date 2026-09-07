package br.com.walletzen.outbox;

import br.com.walletzen.dto.event.EventEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Grava um evento de domínio na tabela {@code outbox_event}. Deve ser chamado
 * <b>dentro</b> da transação que muda o estado ({@code Propagation.MANDATORY}) —
 * é o que garante a atomicidade "mudou o dado ⇒ o evento existe".
 */
@Component
@RequiredArgsConstructor
public class OutboxRecorder {

    static final int ENVELOPE_VERSION = 1;

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String aggregateType, String aggregateId, String eventType, String topic, Object data) {
        UUID eventId = UUID.randomUUID();
        EventEnvelope envelope = new EventEnvelope(
                eventId, eventType, ENVELOPE_VERSION, Instant.now(), aggregateType, aggregateId, data);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize outbox event " + eventType, e);
        }

        repository.save(OutboxEvent.builder()
                .eventId(eventId)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .topic(topic)
                .payload(payload)
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .build());

        applicationEventPublisher.publishEvent(new OutboxAppended());
    }
}
