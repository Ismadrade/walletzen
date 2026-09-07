package br.com.walletzen.adapter.outbound.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Publica <b>uma</b> linha da Outbox no Kafka, cada uma na sua transação. Sucesso ⇒
 * {@code published_at}; falha ⇒ {@code attempts++} + {@code last_error} (volta na próxima rodada).
 * Entrega "pelo menos uma vez".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxDispatcher {

    private static final long SEND_TIMEOUT_SECONDS = 10;
    private static final int MAX_ERROR_LEN = 1000;

    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void dispatch(Long id) {
        OutboxEvent event = repository.findByIdForUpdate(id).orElse(null);
        if (event == null || event.getPublishedAt() != null) {
            return;
        }

        try {
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(event.getTopic(), event.getAggregateId(), event.getPayload());
            record.headers().add(new RecordHeader("event-id",
                    event.getEventId().toString().getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("event-type",
                    event.getEventType().getBytes(StandardCharsets.UTF_8)));

            kafkaTemplate.send(record).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            event.setPublishedAt(LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            registerFailure(event, e);
        } catch (Exception e) {
            registerFailure(event, e);
        }
        repository.save(event);
    }

    private void registerFailure(OutboxEvent event, Exception e) {
        event.setAttempts(event.getAttempts() + 1);
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        event.setLastError(msg.length() > MAX_ERROR_LEN ? msg.substring(0, MAX_ERROR_LEN) : msg);
        log.warn("outbox {}: falha ao publicar (tentativa {}) - {}", event.getEventId(), event.getAttempts(), e.toString());
    }
}
