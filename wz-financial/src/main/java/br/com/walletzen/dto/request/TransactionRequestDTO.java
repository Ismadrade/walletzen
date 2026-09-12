package br.com.walletzen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * {@code userId} é opcional e só tem efeito para quem chama com role {@code ADMIN}
 * (lançar em nome de outro usuário). Para um {@code USER} comum, o dono do lançamento
 * é sempre quem está autenticado — ver {@link br.com.walletzen.service.TransactionService#createTransaction}.
 * <p>
 * {@code transactionDate} ({@code yyyy-MM-dd}) também é opcional: na criação, a ausência vira o dia
 * da gravação; na atualização, mantém a data atual. Datas passadas e futuras são aceitas.
 */
public record TransactionRequestDTO(
        UUID userId,
        @NotBlank String transactionType,
        @NotNull @Positive BigDecimal amount,
        String description,
        LocalDate transactionDate) {
}
