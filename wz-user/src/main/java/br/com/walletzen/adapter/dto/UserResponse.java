package br.com.walletzen.adapter.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(UUID id, String name, String cpf, String email, LocalDate birthDate) {
}
