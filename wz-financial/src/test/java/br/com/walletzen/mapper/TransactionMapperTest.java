package br.com.walletzen.mapper;

import br.com.walletzen.domain.Transaction;
import br.com.walletzen.dto.request.TransactionRequestDTO;
import br.com.walletzen.dto.response.TransactionResponseDTO;
import br.com.walletzen.enums.TransactionType;
import br.com.walletzen.exception.InvalidTransactionTypeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapperImpl();

    @Test
    @DisplayName("toEntity parses the transaction type leniently")
    void toEntity() {
        UUID userId = UUID.randomUUID();

        Transaction entity = mapper.toEntity(
                new TransactionRequestDTO(userId, "income", new BigDecimal("10.00"), "Freelance"));

        assertEquals(TransactionType.INCOME, entity.getTransactionType());
        assertEquals(userId, entity.getUserId());
        assertEquals(new BigDecimal("10.00"), entity.getAmount());
        assertEquals("Freelance", entity.getDescription());
    }

    @Test
    @DisplayName("toEntity rejects an unknown transaction type")
    void toEntityInvalidType() {
        assertThrows(InvalidTransactionTypeException.class, () -> mapper.toEntity(
                new TransactionRequestDTO(UUID.randomUUID(), "PIX", new BigDecimal("10.00"), "x")));
    }

    @Test
    @DisplayName("toResponse serialises the enum as its name and keeps recordStatus as a boolean")
    void toResponse() {
        Transaction entity = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .transactionType(TransactionType.EXPENSE)
                .amount(new BigDecimal("42.00"))
                .description("Conta de luz")
                .recordStatus(true)
                .build();

        TransactionResponseDTO dto = mapper.toResponse(entity);

        assertEquals("EXPENSE", dto.transactionType());
        assertTrue(dto.recordStatus());
        assertEquals("Conta de luz", dto.description());
        assertEquals(entity.getId(), dto.id());
    }
}
