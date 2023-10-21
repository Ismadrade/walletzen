package br.com.walletzen.authuser.adapters.out;

import br.com.walletzen.authuser.adapters.out.repository.UserModelRepository;
import br.com.walletzen.authuser.adapters.out.repository.entity.UserModelEntity;
import br.com.walletzen.authuser.adapters.out.repository.mapper.UserModelEntityMapper;
import br.com.walletzen.authuser.application.core.domain.UserModel;
import br.com.walletzen.authuser.application.ports.out.InsertUserModelOutputPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InsertUserModelAdapter implements InsertUserModelOutputPort {

    @Autowired
    private UserModelRepository userModelRepository;

    @Autowired
    private UserModelEntityMapper userModelEntityMapper;

    @Override
    public void insert(UserModel user) {
        UserModelEntity userModelEntity = userModelEntityMapper.toUserModelEntity(user);
        userModelRepository.save(userModelEntity);
    }
}
