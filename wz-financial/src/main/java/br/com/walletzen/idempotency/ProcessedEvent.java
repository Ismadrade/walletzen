package br.com.walletzen.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Marca de "este eventId já foi processado por este consumidor". Ver {@link IdempotencyGuard}. */
@Entity
@Table(name = "processed_event")
@IdClass(ProcessedEventId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Id
    @Column(nullable = false, updatable = false)
    private String consumer;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
