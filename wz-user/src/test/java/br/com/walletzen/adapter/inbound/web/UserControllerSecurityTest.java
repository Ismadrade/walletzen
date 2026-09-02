package br.com.walletzen.adapter.inbound.web;

import br.com.walletzen.adapter.inbound.web.mapper.UserWebMapperImpl;
import br.com.walletzen.config.SecurityConfig;
import br.com.walletzen.core.domain.PageInfo;
import br.com.walletzen.core.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, UserWebMapperImpl.class})
@Import(SecurityConfig.class)
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder; // exigido pelo filter chain; nunca chamado (jwt() post-processor)

    @Test
    @DisplayName("GET sem token -> 401")
    void getWithoutToken() throws Exception {
        mockMvc.perform(get("")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET com token (qualquer usuário) -> 200")
    void getWithToken() throws Exception {
        when(userService.getAllUsers(any()))
                .thenReturn(new PageInfo<>(List.of(), 0, 10, 0, 0, true));
        mockMvc.perform(get("").with(jwt())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE com role USER -> 403")
    void deleteAsUser() throws Exception {
        mockMvc.perform(delete("/" + UUID.randomUUID())
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE com role ADMIN -> 200")
    void deleteAsAdmin() throws Exception {
        mockMvc.perform(delete("/" + UUID.randomUUID())
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }
}
