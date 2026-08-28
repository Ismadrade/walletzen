package br.com.walletzen.core.port.input;

import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.exception.UserNotFoundException;

import java.util.UUID;

public interface EditUserUseCase {
    void editUser(UUID userId, User user) throws UserNotFoundException;
}
