package br.com.walletzen.core.domain.event;

import java.util.UUID;

public class UserDeletedEvent {

    private UUID userId;

    public UserDeletedEvent(UUID userId) {
        this.userId = userId;
    }

    public UserDeletedEvent() {
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
