package br.com.walletzen.core.exception;

public class UserFieldAlreadyExistsException  extends RuntimeException{

    public UserFieldAlreadyExistsException(String field, String fieldValue) {
        super(field + " is already registered: " + fieldValue);
    }
}
