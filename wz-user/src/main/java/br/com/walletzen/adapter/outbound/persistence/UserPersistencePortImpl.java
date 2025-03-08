package br.com.walletzen.adapter.outbound.persistence;

import br.com.walletzen.adapter.mapper.UserMapper;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.port.output.UserPersistencePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserPersistencePortImpl implements UserPersistencePort {

    private final UserJpaRepository userJpaRepository;

    private final UserMapper userMapper;

    public UserPersistencePortImpl(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }


    @Override
    public List<User> findAll() {
        return userJpaRepository
                .findAll()
                .stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(User user) {
        userJpaRepository.save(userMapper.toEntity(user));
    }
}
