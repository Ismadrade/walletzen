package br.com.walletzen.enums;

import br.com.walletzen.exception.InvalidTransactionTypeException;

public enum TransactionType {
    INCOME, EXPENSE;

    public static TransactionType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidTransactionTypeException(value);
        }
        try {
            return TransactionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidTransactionTypeException(value);
        }
    }
}
