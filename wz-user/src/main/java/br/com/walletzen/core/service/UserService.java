package br.com.walletzen.core.service;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.PageQuery;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.domain.event.UserDeletedEvent;
import br.com.walletzen.core.exception.UserFieldAlreadyExistsException;
import br.com.walletzen.core.exception.UserNotFoundException;
import br.com.walletzen.core.port.input.CreateUserUseCase;
import br.com.walletzen.core.port.input.DeleteUserUseCase;
import br.com.walletzen.core.port.input.EditUserUseCase;
import br.com.walletzen.core.port.input.GetUserUseCase;
import br.com.walletzen.core.port.output.UserDeletedEventPublisherPort;
import br.com.walletzen.core.port.output.UserPersistencePort;

import java.util.UUID;

public class UserService implements GetUserUseCase, CreateUserUseCase, EditUserUseCase, DeleteUserUseCase {

    private final UserPersistencePort userRepository;
    private final UserDeletedEventPublisherPort eventPublisher;

    public UserService(UserPersistencePort userRepository, UserDeletedEventPublisherPort eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PageInfo<User> getAllUsers(PageQuery pageQuery) {
        return userRepository.findAll(pageQuery);
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
        User existingUser = userRepository.findById(userId);

        if (!existingUser.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
            throw new UserFieldAlreadyExistsException("email", user.getEmail());
        }

        existingUser.setEmail(user.getEmail());
        existingUser.setBirthDate(user.getBirthDate());
        existingUser.setName(user.getName());

        userRepository.save(existingUser);

    }

    @Override
    public void deleteUser(UUID userId) throws UserNotFoundException {
        User user = userRepository.findById(userId);
        user.setRecordStatus(false);
        userRepository.save(user);

        eventPublisher.publish(new UserDeletedEvent(user.getId()));

    }
}
