package br.com.walletzen.dto.response;

import java.math.BigDecimal;
import java.util.UUID;


public record TransactionResponseDTO(UUID id, UUID userId, String transactionType, BigDecimal amount, String description, boolean recordStatus) {
}
