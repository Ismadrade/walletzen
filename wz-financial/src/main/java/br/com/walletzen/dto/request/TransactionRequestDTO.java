package br.com.walletzen.dto.request;

import java.math.BigDecimal;
import java.util.UUID;


public record TransactionRequestDTO(UUID userId, String transactionType, BigDecimal amount, String description) {


}
