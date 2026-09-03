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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        u.setName("João");
        u.setCpf("12345678900");
        u.setEmail("email@gteste.com");
        u.setBirthDate(LocalDate.of(1995, 5, 2));
        return u;
    }

    private void noExistingRows() {
        when(userPersistencePort.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userPersistencePort.findByCpf(anyString())).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Should return a user by ID")
    void shouldReturnUserById() {
        UUID userId = UUID.randomUUID();
        User expectedUser = new User(userId, "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), true);
        when(userPersistencePort.findById(userId)).thenReturn(expectedUser);

        assertEquals(userId, userService.getUserById(userId).getId());
    }

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUser() {
        List<User> users = List.of(
                new User(UUID.randomUUID(), "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), true),
                new User(UUID.randomUUID(), "Maria", "98765432100", "email2@gteste.com", LocalDate.of(1997, 10, 25), true)
        );
        when(userPersistencePort.findAll(any())).thenReturn(new PageInfo<>(users, 0, 2, 2, 1, true));

        assertEquals(2, userService.getAllUsers(new PageQuery(0, 2, "name", "ASC")).getContent().size());
    }

    @Test
    @DisplayName("Create novo: gera id, provisiona no Keycloak (username=id) e guarda keycloak_id")
    void shouldCreateFreshUser() {
        noExistingRows();
        User user = newUser();
        when(identityProvider.createUser(anyString(), eq("email@gteste.com"), eq("João"), eq(""), eq("s3nha")))
                .thenReturn("kc-123");

        userService.createUser(user, "s3nha");

        verify(userPersistencePort).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertNotNull(saved.getId());
        assertEquals("kc-123", saved.getKeycloakId());
        verify(identityProvider).createUser(eq(saved.getId().toString()), eq("email@gteste.com"), eq("João"), eq(""), eq("s3nha"));
    }

    @Test
    @DisplayName("Create com email de linha INATIVA: reativa (não cria de novo no Keycloak)")
    void shouldReactivateInactiveUser() {
        User inactive = new User(UUID.randomUUID(), "João Antigo", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), false);
        inactive.setKeycloakId("kc-42");
        when(userPersistencePort.findByEmail("email@gteste.com")).thenReturn(Optional.of(inactive));
        when(userPersistencePort.findByCpf("12345678900")).thenReturn(Optional.of(inactive));

        userService.createUser(newUser(), "novaSenha");

        verify(identityProvider).enableUser("kc-42");
        verify(identityProvider).resetPassword("kc-42", "novaSenha");
        verify(identityProvider).updateUser("kc-42", "email@gteste.com", "João", "");
        verify(identityProvider, never()).createUser(any(), any(), any(), any(), any());
        verify(userPersistencePort).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRecordStatus());
    }

    @Test
    @DisplayName("Create com email de linha ATIVA -> UserFieldAlreadyExistsException")
    void shouldThrowWhenActiveEmailExists() {
        User active = new User(UUID.randomUUID(), "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), true);
        when(userPersistencePort.findByEmail("email@gteste.com")).thenReturn(Optional.of(active));
        when(userPersistencePort.findByCpf(anyString())).thenReturn(Optional.empty());

        var ex = assertThrows(UserFieldAlreadyExistsException.class, () -> userService.createUser(newUser(), "s3nha"));
        assertEquals("email is already registered: email@gteste.com", ex.getMessage());
        verify(userPersistencePort, never()).save(any());
        verify(identityProvider, never()).createUser(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Create com senha em branco -> IllegalArgumentException, nada é tocado")
    void shouldRejectBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(newUser(), "   "));
        verifyNoInteractions(identityProvider);
        verify(userPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Se o save falha depois do Keycloak, compensa deletando o usuário lá")
    void shouldCompensateWhenSaveFails() {
        noExistingRows();
        when(identityProvider.createUser(any(), any(), any(), any(), any())).thenReturn("kc-999");
        doThrow(new RuntimeException("db down")).when(userPersistencePort).save(any());

        assertThrows(RuntimeException.class, () -> userService.createUser(newUser(), "s3nha"));

        verify(identityProvider).deleteUser("kc-999");
    }

    @Test
    @DisplayName("Edit propaga email/nome para o Keycloak quando há keycloak_id")
    void shouldEditUserAndSyncKeycloak() {
        UUID userId = UUID.randomUUID();
        User edited = new User();
        edited.setName("João da Silva");
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
