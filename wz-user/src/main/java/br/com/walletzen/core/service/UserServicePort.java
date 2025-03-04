package br.com.walletzen.core.service;

import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.port.input.GetAllUsersUseCase;
import br.com.walletzen.core.port.output.UserPersistencePort;

import java.util.List;

public class UserServicePort implements GetAllUsersUseCase {

    private final UserPersistencePort userRepository;

    public UserServicePort(UserPersistencePort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
