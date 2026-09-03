package br.com.walletzen.adapter.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record UserRequest(String name,
                          String cpf,
                          String email,
                          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
                          LocalDate birthDate,
                          // write-only: usado só no POST para provisionar o login no Keycloak; nunca é retornado
                          @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
                          String password) {
}
