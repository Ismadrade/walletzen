package br.com.walletzen.adapter.inbound.web.mapper;

import br.com.walletzen.adapter.inbound.web.dto.UserRequest;
import br.com.walletzen.adapter.inbound.web.dto.UserResponse;
import br.com.walletzen.core.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserWebMapper {

    User toDomain(UserRequest userRequest);

    UserResponse toResponse(User user);
}
