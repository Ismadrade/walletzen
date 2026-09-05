package br.com.walletzen.handler;

import br.com.walletzen.dto.response.ExceptionResponse;
import br.com.walletzen.exception.InvalidFilterException;
import br.com.walletzen.exception.InvalidTransactionTypeException;
import br.com.walletzen.exception.TransactionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(new ExceptionResponse(LocalDateTime.now(), "Access denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleTransactionNotFound(TransactionNotFoundException ex) {
        return new ResponseEntity<>(new ExceptionResponse(LocalDateTime.now(), ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidTransactionTypeException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidTransactionType(InvalidTransactionTypeException ex) {
        return new ResponseEntity<>(new ExceptionResponse(LocalDateTime.now(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFilterException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidFilter(InvalidFilterException ex) {
        return new ResponseEntity<>(new ExceptionResponse(LocalDateTime.now(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(new ExceptionResponse(LocalDateTime.now(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ResponseEntity<>(new ExceptionResponse(LocalDateTime.now(), message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ExceptionResponse> handleMalformedRequest(Exception ex) {
        return new ResponseEntity<>(new ExceptionResponse(LocalDateTime.now(), "Malformed request"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return new ResponseEntity<>(new ExceptionResponse(LocalDateTime.now(), "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
