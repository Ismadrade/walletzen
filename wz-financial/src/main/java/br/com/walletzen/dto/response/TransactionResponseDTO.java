package br.com.walletzen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * {@code createdAt} é a data do lançamento exibida na listagem e a mesma usada
 * pelo filtro de período (`year`/`month`) em {@code GET /transactions/user/{id}}.
 */
public record TransactionResponseDTO(UUID id,
                                     UUID userId,
                                     String transactionType,
                                     BigDecimal amount,
                                     String description,
                                     LocalDateTime createdAt,
                                     boolean recordStatus) {
}
