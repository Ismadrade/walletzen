package br.com.walletzen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Liga o agendamento (poller da Outbox + limpeza) e a execução assíncrona
 * (gatilho pós-commit do {@code OutboxPoller}). {@code OutboxProperties} é lido
 * pelo {@code @ConfigurationPropertiesScan} da classe de aplicação.
 */
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {
}
