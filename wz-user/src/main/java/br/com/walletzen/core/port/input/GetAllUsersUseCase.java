package br.com.walletzen.core.port.input;

import br.com.walletzen.core.domain.User;

import java.util.List;

public interface GetAllUsersUseCase {
    List<User> getAllUsers();
}
