package br.com.walletzen.consumer;

import br.com.walletzen.dto.event.EventEnvelope;
import br.com.walletzen.dto.event.UserDeletedData;
import br.com.walletzen.idempotency.IdempotencyGuard;
import br.com.walletzen.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Consome {@code UserDeleted} e desativa as transações do usuário.
 *
 * <ul>
 *   <li>{@link RetryableTopic}: em erro transitório a mensagem vai para
 *       {@code <topic>-retry-0/1/2} (não-bloqueante) com backoff exponencial e, esgotadas
 *       as tentativas, para {@code <topic>-dlt}. Payload malformado ({@link JsonProcessingException})
 *       vai <b>direto</b> para a DLT — não adianta reintentar.</li>
 *   <li>{@link Transactional}: o {@code deleteByUserId} e a marca de idempotência commitam
 *       juntos; se o handler falhar, o rollback desfaz a marca e o retry reprocessa.</li>
 *   <li>{@link IdempotencyGuard}: entrega at-least-once ⇒ a mesma mensagem pode chegar
 *       de novo; a segunda vez é no-op.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedConsumer {

    static final String CONSUMER_ID = "wz-financial:user-deleted";

    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;
    private final IdempotencyGuard idempotencyGuard;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            exclude = JsonProcessingException.class)
    @KafkaListener(topics = "${spring.kafka.topic.wz-user-deleted}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consume(String message) throws JsonProcessingException {
        EventEnvelope envelope = objectMapper.readValue(message, EventEnvelope.class);

        if (!idempotencyGuard.firstTime(envelope.eventId(), CONSUMER_ID)) {
            log.info("Evento {} já processado, ignorando", envelope.eventId());
            return;
        }

        UUID userId = objectMapper.convertValue(envelope.data(), UserDeletedData.class).userId();
        log.info("Received deletion event {} for userId: {}", envelope.eventId(), userId);
        transactionService.deleteByUserId(userId);
    }

    @DltHandler
    public void dlt(String message, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage) {
        log.error("Mensagem parada na DLT: {} | erro: {}", message, errorMessage);
    }
}
