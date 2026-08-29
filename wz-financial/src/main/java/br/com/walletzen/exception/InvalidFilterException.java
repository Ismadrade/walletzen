package br.com.walletzen.exception;

public class InvalidFilterException extends RuntimeException {
    public InvalidFilterException(String message) {
        super(message);
    }
}
