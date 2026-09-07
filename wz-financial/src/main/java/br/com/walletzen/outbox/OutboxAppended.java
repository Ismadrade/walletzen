package br.com.walletzen.outbox;

/**
 * Evento de aplicação publicado pelo {@link OutboxRecorder} após gravar uma linha.
 * O {@link OutboxPoller} escuta em {@code AFTER_COMMIT} para publicar na hora,
 * sem esperar o {@code fixedDelay}.
 */
public record OutboxAppended() {
}
