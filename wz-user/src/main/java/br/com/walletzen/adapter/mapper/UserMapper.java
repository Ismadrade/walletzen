package br.com.walletzen.adapter.mapper;

import br.com.walletzen.adapter.dto.UserRequest;
import br.com.walletzen.adapter.dto.UserResponse;
import br.com.walletzen.adapter.outbound.persistence.entities.UserEntity;
import br.com.walletzen.core.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

    User toDomain(UserEntity entity);

    UserResponse toRecord(User user);

    UserEntity toEntity(User user);

    User toDomain(UserRequest userRequest);

}
