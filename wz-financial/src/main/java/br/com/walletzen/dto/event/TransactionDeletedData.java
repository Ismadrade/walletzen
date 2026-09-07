package br.com.walletzen.dto.event;

import java.util.UUID;

/** {@code data} de {@code TransactionDeleted} — só o necessário para o consumidor reagir. */
public record TransactionDeletedData(UUID id, UUID userId) {
}
