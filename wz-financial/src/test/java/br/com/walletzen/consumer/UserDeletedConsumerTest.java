package br.com.walletzen.consumer;

import br.com.walletzen.dto.event.EventEnvelope;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserDeletedConsumerTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Mock
    private TransactionService transactionService;

    private UserDeletedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UserDeletedConsumer(objectMapper, transactionService);
    }

    private String userDeletedEnvelope(UUID userId) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new EventEnvelope(
                UUID.randomUUID(), "UserDeleted", 1, Instant.now(), "User", userId.toString(),
                Map.of("userId", userId.toString())));
    }

    @Test
    @DisplayName("Um envelope UserDeleted válido dispara o soft delete das transações do usuário")
    void validEvent() throws Exception {
        UUID userId = UUID.randomUUID();

        consumer.consume(userDeletedEnvelope(userId));

        verify(transactionService).deleteByUserId(userId);
    }

    @Test
    @DisplayName("Payload malformado propaga para o error handler rotear pra DLT")
    void malformedPayload() {
        assertThrows(JsonProcessingException.class, () -> consumer.consume("not-json"));
        verifyNoInteractions(transactionService);
    }
}
