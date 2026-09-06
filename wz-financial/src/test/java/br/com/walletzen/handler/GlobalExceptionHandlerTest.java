package br.com.walletzen.handler;

import br.com.walletzen.dto.response.ExceptionResponse;
import br.com.walletzen.exception.InvalidTransactionTypeException;
import br.com.walletzen.exception.TransactionNotFoundException;
import br.com.walletzen.exception.UnknownUserException;
import br.com.walletzen.exception.UserServiceUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Unhandled exceptions are masked with a generic 500 message")
    void genericIsMasked() {
        ResponseEntity<ExceptionResponse> response = handler.handleGenericException(
                new RuntimeException("jdbc:postgresql://secret-host/db failed for user postgres"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().message());
    }

    @Test
    @DisplayName("TransactionNotFoundException is mapped to 404")
    void notFound() {
        UUID id = UUID.randomUUID();

        ResponseEntity<ExceptionResponse> response =
                handler.handleTransactionNotFound(new TransactionNotFoundException(id));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains(id.toString()));
    }

    @Test
    @DisplayName("InvalidTransactionTypeException is mapped to 400")
    void invalidType() {
        ResponseEntity<ExceptionResponse> response =
                handler.handleInvalidTransactionType(new InvalidTransactionTypeException("PIX"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("PIX"));
    }

    @Test
    @DisplayName("UnknownUserException is mapped to 422")
    void unknownUser() {
        UUID id = UUID.randomUUID();

        ResponseEntity<ExceptionResponse> response =
                handler.handleUnknownUser(new UnknownUserException(id));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains(id.toString()));
    }

    @Test
    @DisplayName("UserServiceUnavailableException is mapped to 503 with a generic message")
    void userServiceUnavailable() {
        ResponseEntity<ExceptionResponse> response = handler.handleUserServiceUnavailable(
                new UserServiceUnavailableException(UUID.randomUUID(), new RuntimeException("connection refused to wz-user")));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("user service unavailable, try again later", response.getBody().message());
    }
}
