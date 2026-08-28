package br.com.walletzen.core.port.output;

import br.com.walletzen.core.domain.event.UserDeletedEvent;

public interface UserDeletedEventPublisherPort {
    void publish(UserDeletedEvent event);
}
