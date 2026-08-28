package br.com.walletzen.adapter.inbound.web.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(UUID id, String name, String cpf, String email, LocalDate birthDate) {
}
