package br.com.walletzen.authuser.adapters.Exceptions.handlers;

import br.com.walletzen.authuser.adapters.Exceptions.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ApiError>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        List<ApiError> errors = new ArrayList<>();
        BindingResult results = ex.getBindingResult();

        for (FieldError e: results.getFieldErrors()) {
            errors.add(ApiError.builder()
                    .field(e.getField())
                    .timestamp(LocalDateTime.now())
                    .value(e.getRejectedValue())
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .message(e.getDefaultMessage()).build());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

}
