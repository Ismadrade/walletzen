package br.com.walletzen.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * Relay da Outbox: varre as linhas não publicadas e delega cada uma ao
 * {@link OutboxDispatcher}. Duas formas de disparo:
 * <ul>
 *   <li>{@code @Scheduled(fixedDelay)} — rede de segurança / backlog;</li>
 *   <li>{@code @TransactionalEventListener(AFTER_COMMIT)} — publica na hora, logo
 *       após o commit que gravou a linha, sem esperar o intervalo.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxRepository repository;
    private final OutboxDispatcher dispatcher;
    private final OutboxProperties properties;

    @Scheduled(fixedDelayString = "${outbox.poll-interval:1000}")
    public void publishPending() {
        repository.findByPublishedAtIsNullOrderByIdAsc(PageRequest.of(0, properties.batchSize()))
                .forEach(event -> dispatcher.dispatch(event.getId()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxAppended(OutboxAppended event) {
        publishPending();
    }

    @Scheduled(cron = "${outbox.purge-cron:0 0 3 * * *}")
    @Transactional
    public void purgePublished() {
        int removed = repository.deletePublishedBefore(LocalDateTime.now().minus(properties.retention()));
        if (removed > 0) {
            log.info("outbox: {} eventos publicados removidos (retenção {})", removed, properties.retention());
        }
    }
}
