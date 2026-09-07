package br.com.walletzen.outbox;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxDispatcherTest {

    @Mock
    private OutboxRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxDispatcher dispatcher;

    private OutboxEvent unpublished() {
        return OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .aggregateType("Transaction")
                .aggregateId(UUID.randomUUID().toString())
                .eventType("TransactionCreated")
                .topic("wz-transaction-events")
                .payload("{}")
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .build();
    }

    @Test
    @DisplayName("send OK marca published_at e zera o erro")
    void publishesSuccessfully() {
        OutboxEvent event = unpublished();
        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        dispatcher.dispatch(1L);

        assertNotNull(event.getPublishedAt());
        assertEquals(0, event.getAttempts());
        verify(repository).save(event);
    }

    @Test
    @DisplayName("send com falha incrementa attempts, grava last_error e não marca published_at")
    void registersFailure() {
        OutboxEvent event = unpublished();
        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));

        dispatcher.dispatch(1L);

        assertNull(event.getPublishedAt());
        assertEquals(1, event.getAttempts());
        assertNotNull(event.getLastError());
        verify(repository).save(event);
    }

    @Test
    @DisplayName("linha já publicada é ignorada (sem enviar de novo)")
    void skipsAlreadyPublished() {
        OutboxEvent event = unpublished();
        event.setPublishedAt(LocalDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(event));

        dispatcher.dispatch(1L);

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
        verify(repository, never()).save(any());
    }
}
