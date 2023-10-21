package br.com.walletzen.authuser.application.core.usecase;

import br.com.walletzen.authuser.application.core.domain.UserModel;
import br.com.walletzen.authuser.application.ports.in.InsertUserInputPort;
import br.com.walletzen.authuser.application.ports.out.InsertUserModelOutputPort;

public class InsertUserModelUseCase implements InsertUserInputPort {

    private final InsertUserModelOutputPort insertUserModelOutputPort;

    public InsertUserModelUseCase(InsertUserModelOutputPort insertUserModelOutputPort) {
        this.insertUserModelOutputPort = insertUserModelOutputPort;
    }

    @Override
    public void insert(UserModel user){
        this.insertUserModelOutputPort.insert(user);
    }

}
