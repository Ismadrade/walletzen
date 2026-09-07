package br.com.walletzen.adapter.outbound.outbox;

import br.com.walletzen.core.domain.event.UserDeletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxUserEventPublisherTest {

    @Mock
    private OutboxRecorder outboxRecorder;

    @Test
    @DisplayName("publish grava um UserDeleted na Outbox (aggregate User, data.userId)")
    void writesToOutbox() {
        OutboxUserEventPublisher publisher = new OutboxUserEventPublisher(outboxRecorder);
        ReflectionTestUtils.setField(publisher, "userDeletedTopic", "wz-user-deleted");
        UUID userId = UUID.randomUUID();

        publisher.publish(new UserDeletedEvent(userId));

        verify(outboxRecorder).record(
                eq("User"), eq(userId.toString()), eq("UserDeleted"), eq("wz-user-deleted"),
                eq(Map.of("userId", userId)));
    }
}
