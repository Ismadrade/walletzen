package br.com.walletzen.controller;

import br.com.walletzen.config.SecurityConfig;
import br.com.walletzen.dto.response.TransactionResponseDTO;
import br.com.walletzen.security.Caller;
import br.com.walletzen.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DisplayName("DELETE com qualquer token autenticado passa pelo filtro (a checagem de dono é no service)")
    void deleteAuthenticated() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/transactions/{id}", id).with(jwt()))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteTransaction(eq(id), any(Caller.class));
    }

    @Test
    @DisplayName("O controller repassa o preferred_username (wz_user.id) e a role ADMIN no Caller")
    void forwardsCallerIdentity() throws Exception {
        UUID id = UUID.randomUUID();
        UUID wzUserId = UUID.randomUUID();

        mockMvc.perform(delete("/transactions/{id}", id)
                        .with(jwt()
                                .jwt(j -> j.claim("preferred_username", wzUserId.toString()))
                                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteTransaction(eq(id),
                argThat(c -> c.admin() && wzUserId.equals(c.wzUserId())));
    }

    @Test
    @DisplayName("POST também repassa o Caller — o dono não vem só do body")
    void createForwardsCallerIdentity() throws Exception {
        UUID wzUserId = UUID.randomUUID();
        when(transactionService.createTransaction(any(), any(Caller.class)))
                .thenReturn(new TransactionResponseDTO(UUID.randomUUID(), wzUserId, "INCOME", BigDecimal.TEN, "x", LocalDate.now(), LocalDateTime.now(), true));

        mockMvc.perform(post("/transactions")
                        .with(jwt().jwt(j -> j.claim("preferred_username", wzUserId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transactionType\":\"INCOME\",\"amount\":10.00,\"description\":\"x\"}"))
                .andExpect(status().isCreated());

        verify(transactionService).createTransaction(any(),
                argThat(c -> !c.admin() && wzUserId.equals(c.wzUserId())));
    }
}
