package br.com.walletzen.adapter.outbound.persistence;

import br.com.walletzen.adapter.outbound.persistence.entities.UserEntity;
import br.com.walletzen.adapter.outbound.persistence.mapper.UserPersistenceMapper;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.PageQuery;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.exception.UserNotFoundException;
import br.com.walletzen.core.port.output.UserPersistencePort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserPersistencePortImpl implements UserPersistencePort {

    private final UserJpaRepository userJpaRepository;

    private final UserPersistenceMapper userPersistenceMapper;

    public UserPersistencePortImpl(UserJpaRepository userJpaRepository, UserPersistenceMapper userPersistenceMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userPersistenceMapper = userPersistenceMapper;
    }


    @Override
    public PageInfo<User> findAll(PageQuery pageQuery) {

        Pageable pageable = PageRequest.of(pageQuery.getPage(), pageQuery.getSize(),
                Sort.by(Sort.Direction.fromString(pageQuery.getDirection()), pageQuery.getSort()));

        Page<UserEntity> userPage = userJpaRepository.findAllActiveUsers(pageable);

        List<User> users = userPage.getContent().stream()
                .map(userPersistenceMapper::toDomain)
                .collect(Collectors.toList());

        return new PageInfo<>(
                users,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );

    }

    @Override
    public void save(User user) {
        userJpaRepository.save(userPersistenceMapper.toEntity(user));
    }

    @Override
    public User findById(UUID id) throws UserNotFoundException {
        return userPersistenceMapper.toDomain(userJpaRepository.findUserById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(userPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByCpf(String cpf) {
        return userJpaRepository.findByCpf(cpf).map(userPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return userJpaRepository.existsByCpf(cpf);
    }

    @Override
    public boolean existsById(UUID userId) {
        return userJpaRepository.existsById(userId);
    }
}
