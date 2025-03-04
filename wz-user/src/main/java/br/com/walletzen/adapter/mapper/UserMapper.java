package br.com.walletzen.adapter.mapper;

import br.com.walletzen.adapter.dto.UserDTO;
import br.com.walletzen.adapter.outbound.persistence.entities.UserEntity;
import br.com.walletzen.core.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

    User toDomain(UserEntity entity);

    UserDTO toRecord(User user);

}
