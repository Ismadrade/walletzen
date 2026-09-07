package br.com.walletzen.consumer;

import br.com.walletzen.dto.event.EventEnvelope;
import br.com.walletzen.dto.event.UserDeletedData;
import br.com.walletzen.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedConsumer {

    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;

    @KafkaListener(topics = "${spring.kafka.topic.wz-user-deleted}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) throws JsonProcessingException {
        EventEnvelope envelope = objectMapper.readValue(message, EventEnvelope.class);
        UUID userId = objectMapper.convertValue(envelope.data(), UserDeletedData.class).userId();

        log.info("Received deletion event {} for userId: {}", envelope.eventId(), userId);
        transactionService.deleteByUserId(userId);
    }
}
