package br.com.walletzen.adapter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record UserRequest(String name,
                          String cpf,
                          String email,
                          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
                          LocalDate birthDate) {
}
