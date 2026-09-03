package br.com.walletzen.core.service;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.PageQuery;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.exception.UserFieldAlreadyExistsException;
import br.com.walletzen.core.port.output.IdentityProviderPort;
import br.com.walletzen.core.port.output.UserDeletedEventPublisherPort;
import br.com.walletzen.core.port.output.UserPersistencePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    UserDeletedEventPublisherPort eventPublisher;

    @Mock
    IdentityProviderPort identityProvider;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private static User newUser() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setName("João");
        u.setCpf("12345678900");
        u.setEmail("email@gteste.com");
        u.setBirthDate(LocalDate.of(1995, 5, 2));
        return u;
    }

    @Test
    @DisplayName("Should return a user by ID")
    void shouldReturnUserById() {
        UUID userId = UUID.randomUUID();
        User expectedUser = new User(userId, "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), true);
        when(userPersistencePort.findById(userId)).thenReturn(expectedUser);

        var result = userService.getUserById(userId);

        assertEquals(userId, result.getId());
        verify(userPersistencePort).findById(userId);
    }

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUser() {
        List<User> users = List.of(
                new User(UUID.randomUUID(), "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), true),
                new User(UUID.randomUUID(), "Maria", "98765432100", "email2@gteste.com", LocalDate.of(1997, 10, 25), true)
        );
        when(userPersistencePort.findAll(any())).thenReturn(new PageInfo<>(users, 0, 2, 2, 1, true));

        var result = userService.getAllUsers(new PageQuery(0, 2, "name", "ASC"));

        assertEquals(2, result.getContent().size());
        verify(userPersistencePort).findAll(any());
    }

    @Test
    @DisplayName("Create provisiona o login no Keycloak e guarda o keycloak_id")
    void shouldCreateUserAndProvisionKeycloak() {
        User user = newUser();
        when(identityProvider.createUser("email@gteste.com", "João", "", "s3nha")).thenReturn("kc-123");

        userService.createUser(user, "s3nha");

        verify(identityProvider).createUser("email@gteste.com", "João", "", "s3nha");
        verify(userPersistencePort).save(userCaptor.capture());
        assertEquals("kc-123", userCaptor.getValue().getKeycloakId());
    }

    @Test
    @DisplayName("Create com senha em branco -> IllegalArgumentException, nada é criado")
    void shouldRejectBlankPassword() {
        User user = newUser();

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user, "   "));

        verify(identityProvider, never()).createUser(any(), any(), any(), any());
        verify(userPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Se a gravação falha depois do Keycloak, compensa deletando o usuário lá")
    void shouldCompensateWhenSaveFails() {
        User user = newUser();
        when(identityProvider.createUser(any(), any(), any(), any())).thenReturn("kc-999");
        doThrow(new RuntimeException("db down")).when(userPersistencePort).save(any());

        assertThrows(RuntimeException.class, () -> userService.createUser(user, "s3nha"));

        verify(identityProvider).deleteUser("kc-999");
    }

    @Test
    @DisplayName("Throw email already exists when try to save a user")
    void thrownEmailAlreadyExistsWhenTrySaveUser() {
        User user = newUser();
        when(userPersistencePort.existsByEmail(anyString())).thenReturn(true);

        var ex = assertThrows(UserFieldAlreadyExistsException.class, () -> userService.createUser(user, "s3nha"));

        assertEquals("email is already registered: " + user.getEmail(), ex.getMessage());
        verify(identityProvider, never()).createUser(any(), any(), any(), any());
        verify(userPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Throw when cpf already exists")
    void thrownWhenCpfAlreadyExists() {
        User user = newUser();
        when(userPersistencePort.existsByCpf(anyString())).thenReturn(true);

        var ex = assertThrows(UserFieldAlreadyExistsException.class, () -> userService.createUser(user, "s3nha"));

        assertEquals("CPF is already registered: " + user.getCpf(), ex.getMessage());
        verify(userPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Edit propaga email/nome para o Keycloak quando há keycloak_id")
    void shouldEditUserAndSyncKeycloak() {
        UUID userId = UUID.randomUUID();
        User edited = new User();
        edited.setId(userId);
        edited.setName("João da Silva");
        edited.setCpf("12345678900");
        edited.setEmail("email_novo@gteste.com");
        edited.setBirthDate(LocalDate.of(1995, 5, 2));

        User existing = new User(userId, "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), true);
        existing.setKeycloakId("kc-42");
        when(userPersistencePort.findById(userId)).thenReturn(existing);

        userService.editUser(userId, edited);

        verify(userPersistencePort).save(userCaptor.capture());
        assertEquals("email_novo@gteste.com", userCaptor.getValue().getEmail());
        verify(identityProvider).updateUser("kc-42", "email_novo@gteste.com", "João", "da Silva");
    }

    @Test
    @DisplayName("Throw email already exists when try to edit a user")
    void thrownEmailAlreadyExistsWhenTryEditUser() {
        UUID userId = UUID.randomUUID();
        User edited = new User();
        edited.setId(userId);
        edited.setName("João da Silva");
        edited.setCpf("12345678900");
        edited.setEmail("email_novo@gteste.com");
        edited.setBirthDate(LocalDate.of(1995, 5, 2));

        User existing = new User(userId, "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), true);
        when(userPersistencePort.findById(userId)).thenReturn(existing);
        when(userPersistencePort.existsByEmail(anyString())).thenReturn(true);

        var ex = assertThrows(UserFieldAlreadyExistsException.class, () -> userService.editUser(userId, edited));

        assertEquals("email is already registered: " + edited.getEmail(), ex.getMessage());
        verify(userPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Delete faz soft-delete, publica evento e desabilita no Keycloak")
    void shouldDeleteUserAndDisableKeycloak() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), true);
        user.setKeycloakId("kc-7");
        when(userPersistencePort.findById(userId)).thenReturn(user);

        userService.deleteUser(userId);

        verify(userPersistencePort).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().getRecordStatus());
        verify(eventPublisher).publish(any());
        verify(identityProvider).disableUser("kc-7");
    }
}
