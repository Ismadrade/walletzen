package br.com.walletzen.adapter.outbound.kafka;

import br.com.walletzen.core.domain.event.UserDeletedEvent;
import br.com.walletzen.core.port.output.UserDeletedEventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserDeletedKafkaPublisher implements UserDeletedEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.wz-user-deleted}")
    private String userDeletedTopic;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publish(UserDeletedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(userDeletedTopic, message);
        } catch (Exception ex) {
            log.error("Error trying to send data to topic {} with userID {}", userDeletedTopic, event.getUserId(), ex);
        }
    }
}
