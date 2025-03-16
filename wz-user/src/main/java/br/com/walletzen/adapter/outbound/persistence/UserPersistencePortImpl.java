package br.com.walletzen.adapter.outbound.persistence;

import br.com.walletzen.adapter.mapper.UserMapper;
import br.com.walletzen.adapter.outbound.persistence.entities.UserEntity;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.dto.PageRequestDTO;
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

    private final UserMapper userMapper;

    public UserPersistencePortImpl(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }


    @Override
    public PageInfo<User> findAll(PageRequestDTO pageRequestDTO) {

        Pageable pageable = PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize(),
                Sort.by(Sort.Direction.fromString(pageRequestDTO.getDirection()), pageRequestDTO.getSort()));

        Page<UserEntity> userPage = userJpaRepository.findAll(pageable);

        List<User> users = userPage.getContent().stream()
                .map(userMapper::toDomain)
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
        userJpaRepository.save(userMapper.toEntity(user));
    }

    @Override
    public User findById(UUID id) throws UserNotFoundException {
        return userMapper.toDomain(userJpaRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
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
