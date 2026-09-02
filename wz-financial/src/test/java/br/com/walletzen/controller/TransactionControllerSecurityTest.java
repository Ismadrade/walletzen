package br.com.walletzen.controller;

import br.com.walletzen.config.SecurityConfig;
import br.com.walletzen.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
class TransactionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtDecoder jwtDecoder; // exigido pelo filter chain; nunca chamado (jwt() post-processor)

    @Test
    @DisplayName("GET sem token -> 401")
    void getWithoutToken() throws Exception {
        mockMvc.perform(get("/transactions/{id}", UUID.randomUUID())).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE com role USER -> 403")
    void deleteAsUser() throws Exception {
        mockMvc.perform(delete("/transactions/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE com role ADMIN -> 204")
    void deleteAsAdmin() throws Exception {
        mockMvc.perform(delete("/transactions/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }
}
