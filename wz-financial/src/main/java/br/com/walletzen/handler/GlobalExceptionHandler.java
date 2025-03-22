package br.com.walletzen.handler;

import br.com.walletzen.dto.response.ExceptionResponse;
import br.com.walletzen.exception.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<br.com.walletzen.dto.response.ExceptionResponse> handleUserNotFoundException(TransactionNotFoundException ex) {
        return new ResponseEntity<>(new ExceptionResponse(LocalDateTime.now(), ex.getMessage()), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        return new ResponseEntity<>(new ExceptionResponse(LocalDateTime.now(), ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
