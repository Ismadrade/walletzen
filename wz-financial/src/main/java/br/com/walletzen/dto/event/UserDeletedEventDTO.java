package br.com.walletzen.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Consumer-side view of the {@code wz-user-deleted} Kafka event. The contract is
 * owned by {@code wz-user} ({@code br.com.walletzen.core.domain.event.UserDeletedEvent});
 * unknown fields are ignored so the producer can add attributes without breaking
 * this service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDeletedEventDTO {
    private UUID userId;
}
