package br.com.walletzen.authuser.application.ports.out;

import br.com.walletzen.authuser.application.core.domain.UserModel;

public interface InsertUserModelOutputPort {

    void insert(UserModel user);
}
