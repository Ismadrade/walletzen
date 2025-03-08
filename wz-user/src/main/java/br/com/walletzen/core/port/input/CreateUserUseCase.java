package br.com.walletzen.core.port.input;

import br.com.walletzen.core.domain.User;

public interface CreateUserUseCase {

    void createUser(User user);
}
