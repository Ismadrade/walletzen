package br.com.walletzen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;


public record TransactionRequestDTO(
        @NotNull UUID userId,
        @NotBlank String transactionType,
        @NotNull @Positive BigDecimal amount,
        String description) {


}
