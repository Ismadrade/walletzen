package br.com.walletzen.exception;

import br.com.walletzen.enums.TransactionType;

import java.util.Arrays;
import java.util.stream.Collectors;

public class InvalidTransactionTypeException extends RuntimeException {
    public InvalidTransactionTypeException(String value) {
        super("Invalid transaction type: " + value + ". Allowed values: "
                + Arrays.stream(TransactionType.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")));
    }
}
