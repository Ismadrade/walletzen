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
import br.com.walletzen.core.port.output.IdentityProviderPort;
import br.com.walletzen.core.port.output.UserDeletedEventPublisherPort;
import br.com.walletzen.core.port.output.UserPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class UserService implements GetUserUseCase, CreateUserUseCase, EditUserUseCase, DeleteUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserPersistencePort userRepository;
    private final UserDeletedEventPublisherPort eventPublisher;
    private final IdentityProviderPort identityProvider;

    public UserService(UserPersistencePort userRepository,
                       UserDeletedEventPublisherPort eventPublisher,
                       IdentityProviderPort identityProvider) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.identityProvider = identityProvider;
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
    public void createUser(User user, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserFieldAlreadyExistsException("email", user.getEmail());
        }
        if (userRepository.existsByCpf(user.getCpf())) {
            throw new UserFieldAlreadyExistsException("CPF", user.getCpf());
        }

        // Keycloak primeiro: se a gravação da linha falhar, compensamos removendo o usuário lá.
        String[] name = splitName(user.getName());
        String keycloakId = identityProvider.createUser(user.getEmail(), name[0], name[1], rawPassword);
        user.setKeycloakId(keycloakId);

        try {
            userRepository.save(user);
        } catch (RuntimeException e) {
            identityProvider.deleteUser(keycloakId);
            throw e;
        }
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

        if (existingUser.getKeycloakId() != null) {
            try {
                String[] name = splitName(existingUser.getName());
                identityProvider.updateUser(existingUser.getKeycloakId(), existingUser.getEmail(), name[0], name[1]);
            } catch (RuntimeException e) {
                log.error("Usuário {} atualizado no banco, mas falhou a sincronização com o Keycloak", userId, e);
            }
        }
    }

    @Override
    public void deleteUser(UUID userId) throws UserNotFoundException {
        User user = userRepository.findById(userId);
        user.setRecordStatus(false);
        userRepository.save(user);

        eventPublisher.publish(new UserDeletedEvent(user.getId()));

        if (user.getKeycloakId() != null) {
            try {
                identityProvider.disableUser(user.getKeycloakId());
            } catch (RuntimeException e) {
                log.error("Usuário {} removido no banco, mas falhou desabilitar no Keycloak", userId, e);
            }
        }
    }

    private static String[] splitName(String fullName) {
        String trimmed = fullName == null ? "" : fullName.trim();
        int space = trimmed.indexOf(' ');
        if (space < 0) {
            return new String[]{trimmed, ""};
        }
        return new String[]{trimmed.substring(0, space), trimmed.substring(space + 1).trim()};
    }
}
