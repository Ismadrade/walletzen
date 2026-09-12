package br.com.walletzen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * {@code transactionDate} é a data do lançamento escolhida pelo usuário — é ela que a
 * listagem exibe e que o filtro de período ({@code year}/{@code month}) de
 * {@code GET /transactions/user/{id}} usa. {@code createdAt} é só o instante da gravação.
 */
public record TransactionResponseDTO(UUID id,
                                     UUID userId,
                                     String transactionType,
                                     BigDecimal amount,
                                     String description,
                                     LocalDate transactionDate,
                                     LocalDateTime createdAt,
                                     boolean recordStatus) {
}
