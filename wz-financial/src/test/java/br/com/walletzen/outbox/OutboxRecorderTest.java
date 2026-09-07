package br.com.walletzen.outbox;

import br.com.walletzen.dto.event.EventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxRecorderTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Mock
    private OutboxRepository repository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    @DisplayName("record grava uma linha não publicada com o envelope serializado e sinaliza OutboxAppended")
    void recordsRow() throws Exception {
        OutboxRecorder recorder = new OutboxRecorder(repository, objectMapper, applicationEventPublisher);
        UUID aggregateId = UUID.randomUUID();

        recorder.record("Transaction", aggregateId.toString(), "TransactionCreated", "wz-transaction-events",
                Map.of("id", aggregateId.toString(), "amount", 10));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        OutboxEvent row = captor.getValue();

        assertEquals("Transaction", row.getAggregateType());
        assertEquals(aggregateId.toString(), row.getAggregateId());
        assertEquals("TransactionCreated", row.getEventType());
        assertEquals("wz-transaction-events", row.getTopic());
        assertNotNull(row.getEventId());
        assertNotNull(row.getCreatedAt());
        assertNull(row.getPublishedAt());
        assertEquals(0, row.getAttempts());

        EventEnvelope envelope = objectMapper.readValue(row.getPayload(), EventEnvelope.class);
        assertEquals(row.getEventId(), envelope.eventId());
        assertEquals("TransactionCreated", envelope.eventType());
        assertEquals(1, envelope.version());
        assertNotNull(envelope.occurredAt());

        verify(applicationEventPublisher).publishEvent(any(OutboxAppended.class));
    }
}
