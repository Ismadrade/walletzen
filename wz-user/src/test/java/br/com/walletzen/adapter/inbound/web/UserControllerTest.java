package br.com.walletzen.adapter.inbound.web;

import br.com.walletzen.adapter.inbound.web.mapper.UserWebMapperImpl;
import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.exception.UserNotFoundException;
import br.com.walletzen.core.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, UserWebMapperImpl.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;


    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUser() throws Exception {
        // ARRANGE
        List<User> users = List.of(
                new User(UUID.randomUUID(), "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), true),
                new User(UUID.randomUUID(), "Maria", "98765432100", "email2@gteste.com", LocalDate.of(1997, 10, 25), true)
        );
        PageInfo<User> usersPageInfo = new PageInfo<>(users, 0,2, 2, 1, true);
        when(userService.getAllUsers(any())).thenReturn(usersPageInfo);

       // ASSERT + ACT
       mockMvc.perform(get(""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

    }

    @Test
    @DisplayName("Should return a user by ID")
    void shouldReturnUserById() throws Exception {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "João", "12345678900", "email@gteste.com", LocalDate.of(1995, 5, 2), true);
        when(userService.getUserById(any())).thenReturn(user);

        // ASSERT + ACT
        mockMvc.perform(get("/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value(user.getName()));

    }

    @Test
    @DisplayName("Should not found a user by ID")
    void shouldNotFoundAUserById() throws Exception {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(any())).thenThrow(new UserNotFoundException(userId));

        // ASSERT + ACT
        mockMvc.perform(get("/" + userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with ID: " + userId));

    }


}
