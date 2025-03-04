package br.com.walletzen.adapter.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(UUID id, String name, String cpf, String email, LocalDateTime birthDate) {
}
