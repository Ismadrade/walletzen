package br.com.walletzen.core.port.input;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.PageQuery;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.exception.UserNotFoundException;

import java.util.UUID;

public interface GetUserUseCase {
    PageInfo<User> getAllUsers(PageQuery pageQuery);
    User getUserById(UUID userId) throws UserNotFoundException;
}
