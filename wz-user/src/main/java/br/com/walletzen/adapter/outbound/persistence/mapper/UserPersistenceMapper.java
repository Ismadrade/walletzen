package br.com.walletzen.adapter.outbound.persistence.mapper;

import br.com.walletzen.adapter.outbound.persistence.entities.UserEntity;
import br.com.walletzen.core.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    User toDomain(UserEntity entity);

    UserEntity toEntity(User user);
}
