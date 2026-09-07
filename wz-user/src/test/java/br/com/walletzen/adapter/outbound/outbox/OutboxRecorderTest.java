package br.com.walletzen.adapter.outbound.outbox;

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
    @DisplayName("record grava uma linha não publicada com o envelope e sinaliza OutboxAppended")
    void recordsRow() {
        OutboxRecorder recorder = new OutboxRecorder(repository, objectMapper, applicationEventPublisher);
        UUID userId = UUID.randomUUID();

        recorder.record("User", userId.toString(), "UserDeleted", "wz-user-deleted",
                Map.of("userId", userId));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        OutboxEvent row = captor.getValue();

        assertEquals("User", row.getAggregateType());
        assertEquals(userId.toString(), row.getAggregateId());
        assertEquals("UserDeleted", row.getEventType());
        assertEquals("wz-user-deleted", row.getTopic());
        assertNotNull(row.getEventId());
        assertNotNull(row.getPayload());
        assertNull(row.getPublishedAt());
        assertEquals(0, row.getAttempts());

        verify(applicationEventPublisher).publishEvent(any(OutboxAppended.class));
    }
}
