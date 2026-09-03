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

import java.util.Optional;
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

        Optional<User> byEmail = userRepository.findByEmail(user.getEmail());
        Optional<User> byCpf = userRepository.findByCpf(user.getCpf());

        byEmail.filter(User::getRecordStatus).ifPresent(u -> {
            throw new UserFieldAlreadyExistsException("email", user.getEmail());
        });
        byCpf.filter(User::getRecordStatus).ifPresent(u -> {
            throw new UserFieldAlreadyExistsException("CPF", user.getCpf());
        });

        Optional<User> inactive = byEmail.or(() -> byCpf); // aqui, se presente, é inativo
        if (inactive.isPresent()) {
            if (byEmail.isPresent() && byCpf.isPresent()
                    && !byEmail.get().getId().equals(byCpf.get().getId())) {
                throw new UserFieldAlreadyExistsException("email", user.getEmail());
            }
            reactivate(inactive.get(), user, rawPassword);
            return;
        }

        create(user, rawPassword);
    }

    private void create(User user, String rawPassword) {
        UUID id = UUID.randomUUID();
        user.setId(id);
        String[] name = splitName(user.getName());
        String keycloakId = identityProvider.createUser(id.toString(), user.getEmail(), name[0], name[1], rawPassword);
        user.setKeycloakId(keycloakId);
        try {
            userRepository.save(user);
        } catch (RuntimeException e) {
            identityProvider.deleteUser(keycloakId);
            throw e;
        }
    }

    private void reactivate(User existing, User incoming, String rawPassword) {
        String[] name = splitName(incoming.getName());
        // Keycloak primeiro (simétrico ao create); compensa re-desabilitando se o save falhar.
        identityProvider.enableUser(existing.getKeycloakId());
        identityProvider.resetPassword(existing.getKeycloakId(), rawPassword);
        identityProvider.updateUser(existing.getKeycloakId(), incoming.getEmail(), name[0], name[1]);

        existing.setName(incoming.getName());
        existing.setEmail(incoming.getEmail());
        existing.setCpf(incoming.getCpf());
        existing.setBirthDate(incoming.getBirthDate());
        existing.setRecordStatus(true);
        try {
            userRepository.save(existing);
        } catch (RuntimeException e) {
            identityProvider.disableUser(existing.getKeycloakId());
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
