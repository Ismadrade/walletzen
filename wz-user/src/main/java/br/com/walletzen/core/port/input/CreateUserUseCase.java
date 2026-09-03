package br.com.walletzen.core.port.input;

import br.com.walletzen.core.domain.User;

public interface CreateUserUseCase {

    /**
     * Cria a pessoa e provisiona o login no Identity Provider com a senha informada.
     */
    void createUser(User user, String rawPassword);
}
