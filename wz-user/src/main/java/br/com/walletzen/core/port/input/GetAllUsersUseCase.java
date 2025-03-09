package br.com.walletzen.core.port.input;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.port.input.dto.PageRequestDTO;

import java.util.List;

public interface GetAllUsersUseCase {
    PageInfo<User> getAllUsers(PageRequestDTO pageRequestDTO);
}
