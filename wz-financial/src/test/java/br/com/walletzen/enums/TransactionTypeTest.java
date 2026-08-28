package br.com.walletzen.enums;

import br.com.walletzen.exception.InvalidTransactionTypeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTypeTest {

    @Test
    @DisplayName("fromString accepts the canonical names")
    void canonical() {
        assertEquals(TransactionType.INCOME, TransactionType.fromString("INCOME"));
        assertEquals(TransactionType.EXPENSE, TransactionType.fromString("EXPENSE"));
    }

    @Test
    @DisplayName("fromString is case-insensitive and trims surrounding whitespace")
    void lenient() {
        assertEquals(TransactionType.INCOME, TransactionType.fromString("income"));
        assertEquals(TransactionType.EXPENSE, TransactionType.fromString("  Expense "));
    }

    @Test
    @DisplayName("fromString rejects null and blank values")
    void nullOrBlank() {
        assertThrows(InvalidTransactionTypeException.class, () -> TransactionType.fromString(null));
        assertThrows(InvalidTransactionTypeException.class, () -> TransactionType.fromString(""));
        assertThrows(InvalidTransactionTypeException.class, () -> TransactionType.fromString("   "));
    }

    @Test
    @DisplayName("fromString rejects unknown values and lists the allowed ones")
    void unknown() {
        InvalidTransactionTypeException ex =
                assertThrows(InvalidTransactionTypeException.class, () -> TransactionType.fromString("TRANSFER"));

        assertTrue(ex.getMessage().contains("INCOME"));
        assertTrue(ex.getMessage().contains("EXPENSE"));
    }
}
