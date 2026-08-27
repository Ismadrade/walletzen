package br.com.walletzen.core.port.input;

import br.com.walletzen.core.exception.UserNotFoundException;

import java.util.UUID;

public interface DeleteUserUseCase {
    void deleteUser(UUID userId) throws UserNotFoundException;
}
