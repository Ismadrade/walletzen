package br.com.walletzen.core.port.output;

import br.com.walletzen.core.domain.User;

import java.util.List;
import java.util.UUID;

public interface UserPersistencePort {
    List<User> findAll();
    void save(User user);
    User findById(UUID id) throws Exception;
}
