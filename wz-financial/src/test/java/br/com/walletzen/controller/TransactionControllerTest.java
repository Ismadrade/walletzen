package br.com.walletzen.controller;

import br.com.walletzen.dto.request.TransactionRequestDTO;
import br.com.walletzen.dto.response.TransactionResponseDTO;
import br.com.walletzen.exception.InvalidTransactionTypeException;
import br.com.walletzen.exception.TransactionNotFoundException;
import br.com.walletzen.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    private final UUID transactionId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private TransactionResponseDTO response() {
        return new TransactionResponseDTO(transactionId, userId, "INCOME", new BigDecimal("100.00"), "Salário", true);
    }

    private String requestJson(String type, String amount) throws Exception {
        return objectMapper.writeValueAsString(new TransactionRequestDTO(
                userId, type, amount == null ? null : new BigDecimal(amount), "Salário"));
    }

    @Test
    @DisplayName("GET /transactions/user/{userId} returns the user's transactions")
    void getByUser() throws Exception {
        when(transactionService.getTransactionsByUser(userId)).thenReturn(List.of(response(), response()));

        mockMvc.perform(get("/transactions/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /transactions/{id} returns the transaction")
    void getById() throws Exception {
        when(transactionService.getTransactionById(transactionId)).thenReturn(response());

        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.recordStatus").value(true));
    }

    @Test
    @DisplayName("GET /transactions/{id} returns 404 when the transaction is missing")
    void getByIdNotFound() throws Exception {
        when(transactionService.getTransactionById(transactionId))
                .thenThrow(new TransactionNotFoundException(transactionId));

        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString(transactionId.toString())));
    }

    @Test
    @DisplayName("GET /transactions/{id} returns 400 for a malformed id")
    void getByIdMalformed() throws Exception {
        mockMvc.perform(get("/transactions/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transactions creates the transaction and returns 201")
    void create() throws Exception {
        when(transactionService.createTransaction(any())).thenReturn(response());

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("INCOME", "100.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("INCOME"));
    }

    @Test
    @DisplayName("POST /transactions returns 400 when the body fails validation")
    void createInvalidBody() throws Exception {
        String body = """
                {"userId": null, "transactionType": "  ", "amount": -5, "description": "x"}
                """;

        mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).createTransaction(any());
    }

    @Test
    @DisplayName("POST /transactions returns 400 for an unknown transaction type")
    void createInvalidType() throws Exception {
        when(transactionService.createTransaction(any())).thenThrow(new InvalidTransactionTypeException("TRANSFER"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("TRANSFER", "100.00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("TRANSFER")));
    }

    @Test
    @DisplayName("PUT /transactions/{id} updates the transaction")
    void update() throws Exception {
        when(transactionService.updateTransaction(eq(transactionId), any())).thenReturn(response());

        mockMvc.perform(put("/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("EXPENSE", "55.50")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /transactions/{id} returns 404 when the transaction is missing")
    void updateNotFound() throws Exception {
        when(transactionService.updateTransaction(eq(transactionId), any()))
                .thenThrow(new TransactionNotFoundException(transactionId));

        mockMvc.perform(put("/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("INCOME", "10.00")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /transactions/{id} returns 204 and delegates the soft delete")
    void deleteTransaction() throws Exception {
        mockMvc.perform(delete("/transactions/{id}", transactionId))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteTransaction(transactionId);
    }
}
