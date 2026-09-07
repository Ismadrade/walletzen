package br.com.walletzen.dto.event;

import br.com.walletzen.domain.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

/** {@code data} de {@code TransactionCreated} / {@code TransactionUpdated}. */
public record TransactionEventData(
        UUID id,
        UUID userId,
        String transactionType,
        BigDecimal amount,
        String description) {

    public static TransactionEventData of(Transaction t) {
        return new TransactionEventData(
                t.getId(),
                t.getUserId(),
                t.getTransactionType() == null ? null : t.getTransactionType().name(),
                t.getAmount(),
                t.getDescription());
    }
}
