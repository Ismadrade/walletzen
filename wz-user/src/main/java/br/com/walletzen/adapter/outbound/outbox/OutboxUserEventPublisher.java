package br.com.walletzen.adapter.outbound.outbox;

import br.com.walletzen.core.domain.event.UserDeletedEvent;
import br.com.walletzen.core.port.output.UserDeletedEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementação da porta de publicação via <b>Outbox</b>: em vez de mandar direto pro
 * Kafka, grava o evento na tabela {@code outbox_event} — na mesma transação do
 * soft-delete (ver {@code UserService.deleteUser}). Quem publica de fato é o
 * {@link OutboxPoller}.
 */
@Component
@RequiredArgsConstructor
public class OutboxUserEventPublisher implements UserDeletedEventPublisherPort {

    private final OutboxRecorder outboxRecorder;

    @Value("${spring.kafka.topic.wz-user-deleted}")
    private String userDeletedTopic;

    @Override
    public void publish(UserDeletedEvent event) {
        outboxRecorder.record(
                "User",
                event.getUserId().toString(),
                "UserDeleted",
                userDeletedTopic,
                Map.of("userId", event.getUserId()));
    }
}
