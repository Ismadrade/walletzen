package br.com.walletzen.core.port.output;

import br.com.walletzen.core.dto.UserDeletedEventDTO;

public interface UserDeletedEventPublisherPort {
    void publish(UserDeletedEventDTO event);
}
