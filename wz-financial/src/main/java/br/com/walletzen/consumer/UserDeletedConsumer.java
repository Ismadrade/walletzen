package br.com.walletzen.consumer;

import br.com.walletzen.dto.event.UserDeletedEventDTO;
import br.com.walletzen.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedConsumer {

    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;

    @KafkaListener(topics = "${spring.kafka.topic.wz-user-deleted}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) throws JsonProcessingException {
        UserDeletedEventDTO event = objectMapper.readValue(message, UserDeletedEventDTO.class);
        log.info("Received deletion event for userId: {}", event.getUserId());

        transactionService.deleteByUserId(event.getUserId());
    }
}
