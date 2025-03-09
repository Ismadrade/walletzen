package br.com.walletzen.core.service;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.port.input.CreateUserUseCase;
import br.com.walletzen.core.port.input.EditUserUseCase;
import br.com.walletzen.core.port.input.GetAllUsersUseCase;
import br.com.walletzen.core.port.input.dto.PageRequestDTO;
import br.com.walletzen.core.port.output.UserPersistencePort;

import java.util.List;
import java.util.UUID;

public class UserServicePort implements GetAllUsersUseCase, CreateUserUseCase, EditUserUseCase {

    private final UserPersistencePort userRepository;

    public UserServicePort(UserPersistencePort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PageInfo<User> getAllUsers(PageRequestDTO pageRequestDTO) {
        return userRepository.findAll(pageRequestDTO);
    }

    @Override
    public void createUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void editUser(UUID userId, User user) throws Exception {
        User existingUser = userRepository.findById(userId);
        existingUser.setEmail(user.getEmail());
        existingUser.setCpf(user.getCpf());
        existingUser.setBirthDate(user.getBirthDate());
        existingUser.setName(user.getName());

        userRepository.save(existingUser);

    }
}
