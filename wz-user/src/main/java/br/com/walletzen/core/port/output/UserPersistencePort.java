package br.com.walletzen.core.port.output;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.PageQuery;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.exception.UserNotFoundException;

import java.util.UUID;

public interface UserPersistencePort {
    PageInfo<User> findAll(PageQuery pageQuery);
    void save(User user);
    User findById(UUID id) throws UserNotFoundException;

    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsById(UUID userId);
}
