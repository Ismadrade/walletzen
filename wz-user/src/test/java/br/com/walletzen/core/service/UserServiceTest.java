package br.com.walletzen.core.service;

import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.dto.PageRequestDTO;
import br.com.walletzen.core.exception.UserFieldAlreadyExistsException;
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


    @InjectMocks
    private UserServicePort userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;


    @Test
    @DisplayName("Should return a user by ID")
    void shouldReturnUserById() {

        // ARRANGE
        UUID userId = UUID.randomUUID();
        User expectedUser = new User(userId, "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2).toString(), true);

        when(userPersistencePort.findById(userId)).thenReturn(expectedUser);

        // ACT
        var result = userService.getUserById(userId);

        // ASSERT
        assertEquals(userId, result.getId());
        verify(userPersistencePort).findById(userId);
    }

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUser() {

        // ARRANGE
        List<User> users = List.of(
                new User(UUID.randomUUID(), "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2).toString(), true),
                new User(UUID.randomUUID(), "Maria", "98765432100", "email2@gteste.com", LocalDate.of(1997, 10, 25).toString(), true)
        );
        PageInfo<User> usersPageInfo = new PageInfo<>(users, 0,2, 2, 1, true);
        when(userPersistencePort.findAll(any())).thenReturn(usersPageInfo);

        // ACT
        var result = userService.getAllUsers(new PageRequestDTO(0, 2, "name", "ASC"));

        // ASSERT
        assertEquals(result.getContent().size(), 2);
        verify(userPersistencePort).findAll(any());
    }

    @Test
    @DisplayName("Should create a user")
    void shouldCreateUser(){

        //ARRANGE
        User userToBeSaved = new User();
        userToBeSaved.setId(UUID.randomUUID());
        userToBeSaved.setName("João");
        userToBeSaved.setCpf("12345678900");
        userToBeSaved.setEmail("email@gteste.com");
        userToBeSaved.setBirthDate(LocalDate.of(1995, 5, 2).toString());

        // ACT
        userService.createUser(userToBeSaved);

        // ASSERT
        verify(userPersistencePort).save(userToBeSaved);

    }

    @Test
    @DisplayName("Throw email already exists when try to save a user")
    void thrownEmailAlreadyExistsWhenTrySaveUser(){

        //ARRANGE
        User userToBeSaved = new User();
        userToBeSaved.setId(UUID.randomUUID());
        userToBeSaved.setName("João");
        userToBeSaved.setCpf("12345678900");
        userToBeSaved.setEmail("email@gteste.com");
        userToBeSaved.setBirthDate(LocalDate.of(1995, 5, 2).toString());

        when(userPersistencePort.existsByEmail(anyString())).thenReturn(true);

        // ASSERT + ACT
        UserFieldAlreadyExistsException userFieldAlreadyExistsException = assertThrows(UserFieldAlreadyExistsException.class, () -> {
            userService.createUser(userToBeSaved);
        });

        String message = userFieldAlreadyExistsException.getMessage();
        assertEquals("email is already registered: " + userToBeSaved.getEmail(), message);
        verify(userPersistencePort, never()).save(any());

    }

    @Test
    @DisplayName("Throw when cpf already exists")
    void thrownWhenCpfAlreadyExists(){

        //ARRANGE
        User userToBeSaved = new User();
        userToBeSaved.setId(UUID.randomUUID());
        userToBeSaved.setName("João");
        userToBeSaved.setCpf("12345678900");
        userToBeSaved.setEmail("email@gteste.com");
        userToBeSaved.setBirthDate(LocalDate.of(1995, 5, 2).toString());

        when(userPersistencePort.existsByCpf(anyString())).thenReturn(true);

        // ASSERT + ACT
        UserFieldAlreadyExistsException userFieldAlreadyExistsException = assertThrows(UserFieldAlreadyExistsException.class, () -> {
            userService.createUser(userToBeSaved);
        });

        String message = userFieldAlreadyExistsException.getMessage();
        assertEquals("CPF is already registered: " + userToBeSaved.getCpf(), message);
        verify(userPersistencePort, never()).save(any());

    }

    @Test
    @DisplayName("Should edit a user")
    void shouldEditUser(){

        //ARRANGE
        UUID userId = UUID.randomUUID();
        User userEdited = new User();
        userEdited.setId(userId);
        userEdited.setName("João da Silva");
        userEdited.setCpf("12345678900");
        userEdited.setEmail("email_novo@gteste.com");
        userEdited.setBirthDate(LocalDate.of(1995, 5, 2).toString());

        User userToBeEdited = new User(userId, "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2).toString(),true);

        when(userPersistencePort.findById(userId)).thenReturn(userToBeEdited);

        // ACT
        userService.editUser(userId, userEdited);

        // ASSERT
        verify(userPersistencePort).save(userCaptor.capture());
        User userPersisted = userCaptor.getValue();
        assertEquals(userEdited.getName(), userPersisted.getName());
        assertEquals(userEdited.getEmail(), userPersisted.getEmail());

    }

    @Test
    @DisplayName("Throw email already exists when try to edit a user")
    void thrownEmailAlreadyExistsWhenTryEditUser(){

        //ARRANGE
        UUID userId = UUID.randomUUID();
        User userEdited = new User();
        userEdited.setId(userId);
        userEdited.setName("João da Silva");
        userEdited.setCpf("12345678900");
        userEdited.setEmail("email_novo@gteste.com");
        userEdited.setBirthDate(LocalDate.of(1995, 5, 2).toString());

        User userToBeEdited = new User(userId, "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2).toString(),true);

        when(userPersistencePort.findById(userId)).thenReturn(userToBeEdited);
        when(userPersistencePort.existsByEmail(anyString())).thenReturn(true);

        // ASSERT + ACT
        UserFieldAlreadyExistsException userFieldAlreadyExistsException = assertThrows(UserFieldAlreadyExistsException.class, () -> {
            userService.editUser(userId, userEdited);
        });

        String message = userFieldAlreadyExistsException.getMessage();
        assertEquals("email is already registered: " + userEdited.getEmail(), message);
        verify(userPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should delete a user")
    void shouldDeleteUserAndPublishEvent() throws Exception {

        // ARRANGE
        UUID userId = UUID.randomUUID();
        User userToBeDeleted = new User(userId, "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2).toString(), true);

        when(userPersistencePort.findById(userId)).thenReturn(userToBeDeleted);

        // ACT
        userService.deleteUser(userId);

        // ASSERT
        verify(userPersistencePort).save(userCaptor.capture());
        User userDeleted = userCaptor.getValue();
        assertFalse(userDeleted.getRecordStatus());
        verify(userPersistencePort).save(any());
        verify(eventPublisher).publish(any());
    }
}
