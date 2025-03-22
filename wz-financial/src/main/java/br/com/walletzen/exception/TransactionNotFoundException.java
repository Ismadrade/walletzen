package br.com.walletzen.exception;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(UUID transactionId) {
        super("Transaction not found with ID: " + transactionId);
    }
}
