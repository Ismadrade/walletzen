package br.com.walletzen.adapter.inbound.web;

import br.com.walletzen.adapter.inbound.web.mapper.UserWebMapperImpl;
import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.domain.User;
import br.com.walletzen.core.exception.UserNotFoundException;
import br.com.walletzen.core.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, UserWebMapperImpl.class})
@AutoConfigureMockMvc(addFilters = false) // segurança coberta em UserControllerSecurityTest
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
    @DisplayName("POST cria e repassa a senha para o caso de uso")
    void shouldCreateUserForwardingPassword() throws Exception {
        String body = """
                {"name":"João","cpf":"12345678900","email":"joao@x.com","birthDate":"1990-01-01","password":"s3nha"}
                """;

        mockMvc.perform(post("").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        verify(userService).createUser(any(User.class), eq("s3nha"));
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

    private static JwtAuthenticationToken tokenWithUsername(String preferredUsername) {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none")
                .claim("preferred_username", preferredUsername).build();
        return new JwtAuthenticationToken(jwt);
    }

    @Test
    @DisplayName("GET /me resolve o usuário pelo claim preferred_username")
    void meReturnsCurrentUser() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Eva Test", "11122233300", "eva@x.com", LocalDate.of(1990, 1, 1), true);
        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/me").principal(tokenWithUsername(userId.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Eva Test"))
                .andExpect(jsonPath("$.email").value("eva@x.com"));
    }

    @Test
    @DisplayName("GET /me -> 404 quando o token não tem linha em wz_user")
    void meNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/me").principal(tokenWithUsername(userId.toString())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /me -> 404 quando o preferred_username não é UUID (usuário seed do realm)")
    void meSeedUser() throws Exception {
        mockMvc.perform(get("/me").principal(tokenWithUsername("alice")))
                .andExpect(status().isNotFound());

        verify(userService, never()).getUserById(any());
    }
}
