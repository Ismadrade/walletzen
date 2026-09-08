package br.com.walletzen.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dedupe de eventos. Roda <b>na mesma transação</b> do handler ({@code Propagation.MANDATORY}):
 * se o handler falhar e a transação der rollback, a marca some junto e a mensagem é
 * reprocessada normalmente no retry topic.
 *
 * <p>Checa-e-insere (não upsert): a corrida de duas entregas simultâneas do mesmo
 * {@code eventId} é improvável aqui (o Kafka entrega uma partição a um único consumidor
 * do grupo, em sequência) e, se ocorrer, degrada de forma benigna — a segunda transação
 * falha no commit por violação da PK e a mensagem volta pelo retry, onde a marca já existe.
 */
@Component
@RequiredArgsConstructor
public class IdempotencyGuard {

    private final ProcessedEventRepository repository;

    /** {@code true} = primeira vez (siga com o efeito); {@code false} = já processado (pule). */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean firstTime(UUID eventId, String consumer) {
        if (repository.existsById(new ProcessedEventId(eventId, consumer))) {
            return false;
        }
        repository.save(new ProcessedEvent(eventId, consumer, LocalDateTime.now()));
        return true;
    }
}
