package br.com.walletzen.core.service;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.exception.UserFieldAlreadyExistsException;
import br.com.walletzen.core.exception.UserNotFoundException;
import br.com.walletzen.core.port.input.CreateUserUseCase;
import br.com.walletzen.core.port.input.EditUserUseCase;
import br.com.walletzen.core.port.input.GetUserUseCase;
import br.com.walletzen.core.dto.PageRequestDTO;
import br.com.walletzen.core.port.output.UserPersistencePort;

import java.util.UUID;

public class UserServicePort implements GetUserUseCase, CreateUserUseCase, EditUserUseCase {

    private final UserPersistencePort userRepository;

    public UserServicePort(UserPersistencePort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PageInfo<User> getAllUsers(PageRequestDTO pageRequestDTO) {
        return userRepository.findAll(pageRequestDTO);
    }

    @Override
    public User getUserById(UUID userId) throws UserNotFoundException {
        return userRepository.findById(userId);
    }

    @Override
    public void createUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserFieldAlreadyExistsException("email", user.getEmail());
        }
        if (userRepository.existsByCpf(user.getCpf())) {
            throw new UserFieldAlreadyExistsException("CPF", user.getCpf());
        }

        userRepository.save(user);
    }

    @Override
    public void editUser(UUID userId, User user) throws UserNotFoundException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserFieldAlreadyExistsException("email", user.getEmail());
        }
        if (userRepository.existsByCpf(user.getCpf())) {
            throw new UserFieldAlreadyExistsException("CPF", user.getCpf());
        }

        User existingUser = userRepository.findById(userId);

        existingUser.setEmail(user.getEmail());
        existingUser.setCpf(user.getCpf());
        existingUser.setBirthDate(user.getBirthDate());
        existingUser.setName(user.getName());

        userRepository.save(existingUser);

    }
}
