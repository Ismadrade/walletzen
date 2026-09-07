package br.com.walletzen.adapter.outbound.outbox;

/**
 * Evento de aplicação publicado pelo {@link OutboxRecorder} após gravar uma linha;
 * o {@link OutboxPoller} escuta em {@code AFTER_COMMIT} para publicar sem esperar o intervalo.
 */
public record OutboxAppended() {
}
