package br.com.walletzen.adapter.outbound.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuração do relay da Outbox (prefixo {@code outbox}). {@code poll-interval} é
 * lido direto na anotação {@code @Scheduled} do {@link OutboxPoller}.
 */
@ConfigurationProperties(prefix = "outbox")
public record OutboxProperties(
        @DefaultValue("100") int batchSize,
        @DefaultValue("7d") Duration retention,
        @DefaultValue("0 0 3 * * *") String purgeCron) {
}
