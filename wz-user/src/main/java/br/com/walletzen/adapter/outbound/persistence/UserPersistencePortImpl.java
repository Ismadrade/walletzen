package br.com.walletzen.adapter.outbound.persistence;

import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.port.output.UserPersistencePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserPersistencePortImpl implements UserPersistencePort {

    private final UserJpaRepository userJpaRepository;

    public UserPersistencePortImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }


    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream().map(userEntity -> new User(
                userEntity.getId(), userEntity.getName(),
                userEntity.getCpf(), userEntity.getEmail(),
                userEntity.getBirthDate().toString(), userEntity.getCreatedAt().toString(),
                userEntity.getUpdatedAt().toString(), userEntity.getRecordStatus()
        )).collect(Collectors.toList());
    }
}
