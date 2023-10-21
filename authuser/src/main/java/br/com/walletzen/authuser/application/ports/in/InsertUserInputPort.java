package br.com.walletzen.authuser.application.ports.in;

import br.com.walletzen.authuser.application.core.domain.UserModel;

public interface InsertUserInputPort {

    void insert(UserModel userModel);
}
