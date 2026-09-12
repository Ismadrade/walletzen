package br.com.walletzen.dto.response;

import java.math.BigDecimal;

/**
 * Totais das transações ativas de um período, calculados no banco (independem da paginação).
 *
 * @param balance        {@code income - expense}
 * @param averageExpense {@code expense / expenseCount} com 2 casas ({@code 0} sem despesas)
 */
public record TransactionSummaryDTO(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance,
        long expenseCount,
        BigDecimal averageExpense) {
}
