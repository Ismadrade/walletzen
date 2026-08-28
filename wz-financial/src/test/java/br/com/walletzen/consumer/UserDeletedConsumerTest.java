package br.com.walletzen.consumer;

import br.com.walletzen.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserDeletedConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TransactionService transactionService;

    private UserDeletedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UserDeletedConsumer(objectMapper, transactionService);
    }

    @Test
    @DisplayName("A valid event triggers the soft delete of the user's transactions")
    void validEvent() throws Exception {
        UUID userId = UUID.randomUUID();

        consumer.consume("{\"userId\":\"" + userId + "\"}");

        verify(transactionService).deleteByUserId(userId);
    }

    @Test
    @DisplayName("A malformed payload propagates so the error handler can route it to the DLT")
    void malformedPayload() {
        assertThrows(JsonProcessingException.class, () -> consumer.consume("not-json"));
        verifyNoInteractions(transactionService);
    }
}
