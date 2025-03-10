package br.com.walletzen.core.port.input;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.dto.PageRequestDTO;

import java.util.UUID;

public interface GetUserUseCase {
    PageInfo<User> getAllUsers(PageRequestDTO pageRequestDTO);
    User getUserById(UUID userId) throws Exception;
}
