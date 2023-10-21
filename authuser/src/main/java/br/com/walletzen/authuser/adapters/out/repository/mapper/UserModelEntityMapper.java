package br.com.walletzen.authuser.adapters.out.repository.mapper;

import br.com.walletzen.authuser.adapters.out.repository.entity.UserModelEntity;
import br.com.walletzen.authuser.application.core.domain.UserModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserModelEntityMapper {

    UserModelEntity toUserModelEntity(UserModel userModel);
}
