package br.com.walletzen.authuser.adapters.in.controller.mapper;

import br.com.walletzen.authuser.adapters.in.controller.request.UserRequest;
import br.com.walletzen.authuser.application.core.domain.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    UserModel toUser(UserRequest userRequest);
}
