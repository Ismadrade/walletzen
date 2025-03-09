package br.com.walletzen.core.port.output;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.port.input.dto.PageRequestDTO;

import java.util.List;
import java.util.UUID;

public interface UserPersistencePort {
    PageInfo<User> findAll(PageRequestDTO pageRequestDTO);
    void save(User user);
    User findById(UUID id) throws Exception;
}
