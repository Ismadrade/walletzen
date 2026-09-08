package br.com.walletzen.consumer;

import br.com.walletzen.dto.event.EventEnvelope;
import br.com.walletzen.idempotency.IdempotencyGuard;
import br.com.walletzen.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDeletedConsumerTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Mock
    private TransactionService transactionService;

    @Mock
    private IdempotencyGuard idempotencyGuard;

    private UserDeletedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UserDeletedConsumer(objectMapper, transactionService, idempotencyGuard);
    }

    private String userDeletedEnvelope(UUID eventId, UUID userId) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new EventEnvelope(
                eventId, "UserDeleted", 1, Instant.now(), "User", userId.toString(),
                Map.of("userId", userId.toString())));
    }

    @Test
    @DisplayName("Envelope UserDeleted novo dispara o soft delete das transações do usuário")
    void validEvent() throws Exception {
        UUID userId = UUID.randomUUID();
        when(idempotencyGuard.firstTime(any(), eq(UserDeletedConsumer.CONSUMER_ID))).thenReturn(true);

        consumer.consume(userDeletedEnvelope(UUID.randomUUID(), userId));

        verify(transactionService).deleteByUserId(userId);
    }

    @Test
    @DisplayName("Reentrega do mesmo eventId é no-op (idempotência)")
    void duplicateIsSkipped() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String message = userDeletedEnvelope(eventId, userId);
        when(idempotencyGuard.firstTime(eq(eventId), eq(UserDeletedConsumer.CONSUMER_ID)))
                .thenReturn(true, false);

        consumer.consume(message);
        consumer.consume(message);

        verify(transactionService, times(1)).deleteByUserId(userId);
    }

    @Test
    @DisplayName("Payload malformado propaga JsonProcessingException (roteia direto pra DLT)")
    void malformedPayload() {
        assertThrows(JsonProcessingException.class, () -> consumer.consume("not-json"));
        verifyNoInteractions(transactionService);
        verifyNoInteractions(idempotencyGuard);
    }

    @Test
    @DisplayName("DLT handler não estoura")
    void dltHandlerLogs() {
        consumer.dlt("{\"broken\":true}", "erro qualquer");
        verify(transactionService, never()).deleteByUserId(any());
    }
}
