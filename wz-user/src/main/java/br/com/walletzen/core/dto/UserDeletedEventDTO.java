package br.com.walletzen.core.dto;

import java.util.UUID;

public class UserDeletedEventDTO {

    private UUID userId;

    public UserDeletedEventDTO(UUID userId) {
        this.userId = userId;
    }

    public UserDeletedEventDTO() {
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
