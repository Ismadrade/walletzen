package br.com.walletzen.controller;

import br.com.walletzen.dto.request.TransactionRequestDTO;
import br.com.walletzen.dto.response.PageResponseDTO;
import br.com.walletzen.dto.response.TransactionResponseDTO;
import br.com.walletzen.security.Caller;
import br.com.walletzen.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponseDTO<TransactionResponseDTO>> getByUser(
            @PathVariable UUID userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            JwtAuthenticationToken auth) {
        return ResponseEntity.ok(
                transactionService.getTransactionsByUser(userId, year, month, page, size, Caller.from(auth)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getById(@PathVariable UUID id, JwtAuthenticationToken auth) {
        return ResponseEntity.ok(transactionService.getTransactionById(id, Caller.from(auth)));
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(@Valid @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> update(@PathVariable UUID id,
                                                        @Valid @RequestBody TransactionRequestDTO dto,
                                                        JwtAuthenticationToken auth) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, dto, Caller.from(auth)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, JwtAuthenticationToken auth) {
        transactionService.deleteTransaction(id, Caller.from(auth));
        return ResponseEntity.noContent().build();
    }
}
