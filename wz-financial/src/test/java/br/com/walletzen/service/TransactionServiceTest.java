package br.com.walletzen.service;

import br.com.walletzen.client.UserValidationGateway;
import br.com.walletzen.domain.Transaction;
import br.com.walletzen.dto.request.TransactionRequestDTO;
import br.com.walletzen.dto.response.PageResponseDTO;
import br.com.walletzen.dto.response.TransactionResponseDTO;
import br.com.walletzen.enums.TransactionType;
import br.com.walletzen.exception.InvalidFilterException;
import br.com.walletzen.exception.InvalidTransactionTypeException;
import br.com.walletzen.exception.TransactionNotFoundException;
import br.com.walletzen.exception.UnknownUserException;
import br.com.walletzen.exception.UserServiceUnavailableException;
import br.com.walletzen.mapper.TransactionMapper;
import br.com.walletzen.security.Caller;
import br.com.walletzen.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private UserValidationGateway userValidationGateway;

    @InjectMocks
    private TransactionService transactionService;

    private final UUID userId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();
    private final Caller CALLER = new Caller(userId, false); // dono das transações do teste

    private Transaction sampleTransaction() {
        return Transaction.builder()
                .id(transactionId)
                .transactionType(TransactionType.INCOME)
                .amount(new BigDecimal("100.00"))
                .description("Salário")
                .userId(userId)
                .recordStatus(true)
                .build();
    }

    private TransactionResponseDTO sampleResponse() {
        return new TransactionResponseDTO(transactionId, userId, "INCOME", new BigDecimal("100.00"), "Salário", true);
    }

    @Test
    @DisplayName("getTransactionsByUser without a filter queries with an unbounded window (null start/end)")
    void getTransactionsByUserNoFilter() {
        when(transactionRepository.findActiveByUser(eq(userId), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleTransaction(), sampleTransaction())));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(sampleResponse());

        PageResponseDTO<TransactionResponseDTO> result =
                transactionService.getTransactionsByUser(userId, null, null, 0, 10, CALLER);

        assertEquals(2, result.content().size());
        assertEquals(0, result.pageNumber());
        verify(transactionRepository).findActiveByUser(eq(userId), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("getTransactionsByUser with year + month queries the createdAt window [1st of month, 1st of next month)")
    void getTransactionsByUserMonthFilter() {
        ArgumentCaptor<LocalDateTime> start = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> end = ArgumentCaptor.forClass(LocalDateTime.class);
        when(transactionRepository.findActiveByUser(
                eq(userId), start.capture(), end.capture(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleTransaction())));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(sampleResponse());

        transactionService.getTransactionsByUser(userId, 2026, 8, 0, 10, CALLER);

        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0), start.getValue());
        assertEquals(LocalDateTime.of(2026, 9, 1, 0, 0), end.getValue());
    }

    @Test
    @DisplayName("getTransactionsByUser with year only queries the whole calendar year")
    void getTransactionsByUserYearFilter() {
        ArgumentCaptor<LocalDateTime> start = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> end = ArgumentCaptor.forClass(LocalDateTime.class);
        when(transactionRepository.findActiveByUser(
                eq(userId), start.capture(), end.capture(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        transactionService.getTransactionsByUser(userId, 2026, null, 0, 10, CALLER);

        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), start.getValue());
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0), end.getValue());
    }

    @Test
    @DisplayName("getTransactionsByUser rejects a month filter without a year")
    void getTransactionsByUserMonthWithoutYear() {
        assertThrows(InvalidFilterException.class,
                () -> transactionService.getTransactionsByUser(userId, null, 8, 0, 10, CALLER));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("getTransactionsByUser rejects an out-of-range month")
    void getTransactionsByUserInvalidMonth() {
        assertThrows(InvalidFilterException.class,
                () -> transactionService.getTransactionsByUser(userId, 2026, 13, 0, 10, CALLER));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("getTransactionsByUser rejects a negative page")
    void getTransactionsByUserNegativePage() {
        assertThrows(InvalidFilterException.class,
                () -> transactionService.getTransactionsByUser(userId, null, null, -1, 10, CALLER));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("getTransactionsByUser caps the page size at MAX_PAGE_SIZE")
    void getTransactionsByUserCapsPageSize() {
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(transactionRepository.findActiveByUser(eq(userId), isNull(), isNull(), pageable.capture()))
                .thenReturn(new PageImpl<>(List.of()));

        transactionService.getTransactionsByUser(userId, null, null, 0, 500, CALLER);

        assertEquals(TransactionService.MAX_PAGE_SIZE, pageable.getValue().getPageSize());
    }

    @Test
    @DisplayName("getTransactionById returns the transaction when it exists and is active")
    void getTransactionByIdFound() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true))
                .thenReturn(Optional.of(sampleTransaction()));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(sampleResponse());

        TransactionResponseDTO result = transactionService.getTransactionById(transactionId, CALLER);

        assertEquals(transactionId, result.id());
    }

    @Test
    @DisplayName("getTransactionById throws TransactionNotFoundException when nothing matches")
    void getTransactionByIdNotFound() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransactionById(transactionId, CALLER));
    }

    @Test
    @DisplayName("createTransaction forces recordStatus = true before persisting")
    void createTransaction() {
        TransactionRequestDTO dto = new TransactionRequestDTO(userId, "INCOME", new BigDecimal("100.00"), "Salário");
        Transaction mapped = Transaction.builder()
                .transactionType(TransactionType.INCOME)
                .amount(new BigDecimal("100.00"))
                .build();
        when(transactionMapper.toEntity(dto)).thenReturn(mapped);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(sampleResponse());

        transactionService.createTransaction(dto, CALLER);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertTrue(captor.getValue().isRecordStatus());
    }

    @Test
    @DisplayName("createTransaction para um USER ignora o userId do body e usa o do token")
    void createTransactionUserOwnerComesFromToken() {
        UUID someoneElseId = UUID.randomUUID();
        TransactionRequestDTO dto = new TransactionRequestDTO(someoneElseId, "INCOME", new BigDecimal("100.00"), "x");
        when(transactionMapper.toEntity(dto)).thenReturn(Transaction.builder().transactionType(TransactionType.INCOME).build());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(sampleResponse());

        transactionService.createTransaction(dto, CALLER); // CALLER.wzUserId() == userId

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
    }

    @Test
    @DisplayName("createTransaction para um ADMIN com userId no body cria em nome daquele usuário")
    void createTransactionAdminCanTargetAnotherUser() {
        UUID target = UUID.randomUUID();
        TransactionRequestDTO dto = new TransactionRequestDTO(target, "INCOME", new BigDecimal("100.00"), "x");
        when(transactionMapper.toEntity(dto)).thenReturn(Transaction.builder().transactionType(TransactionType.INCOME).build());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(sampleResponse());

        transactionService.createTransaction(dto, ADMIN); // ADMIN.wzUserId() != target

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertEquals(target, captor.getValue().getUserId());
    }

    @Test
    @DisplayName("createTransaction sem como determinar o dono -> IllegalArgumentException")
    void createTransactionWithoutOwner() {
        Caller callerSemWzUserId = new Caller(null, false); // ex.: usuário seed sem vínculo em wz_user
        TransactionRequestDTO dto = new TransactionRequestDTO(null, "INCOME", new BigDecimal("100.00"), "x");

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(dto, callerSemWzUserId));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTransaction valida o dono resolvido em wz-user antes de gravar")
    void createTransactionValidatesOwner() {
        TransactionRequestDTO dto = new TransactionRequestDTO(null, "INCOME", new BigDecimal("10.00"), "x");
        when(transactionMapper.toEntity(dto)).thenReturn(Transaction.builder().transactionType(TransactionType.INCOME).build());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(sampleResponse());

        transactionService.createTransaction(dto, CALLER);

        verify(userValidationGateway).assertActiveUser(userId);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("createTransaction rejeita usuário inexistente/inativo sem gravar (422)")
    void createTransactionRejectsUnknownUser() {
        UUID target = UUID.randomUUID();
        TransactionRequestDTO dto = new TransactionRequestDTO(target, "INCOME", new BigDecimal("10.00"), "x");
        doThrow(new UnknownUserException(target)).when(userValidationGateway).assertActiveUser(target);

        assertThrows(UnknownUserException.class, () -> transactionService.createTransaction(dto, ADMIN));
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(transactionMapper);
    }

    @Test
    @DisplayName("createTransaction propaga indisponibilidade de wz-user (503), sem gravar")
    void createTransactionPropagatesServiceUnavailable() {
        TransactionRequestDTO dto = new TransactionRequestDTO(null, "INCOME", new BigDecimal("10.00"), "x");
        doThrow(new UserServiceUnavailableException(userId, new RuntimeException("boom")))
                .when(userValidationGateway).assertActiveUser(userId);

        assertThrows(UserServiceUnavailableException.class, () -> transactionService.createTransaction(dto, CALLER));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTransaction changes type, amount and description of an active transaction")
    void updateTransaction() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true))
                .thenReturn(Optional.of(sampleTransaction()));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(sampleResponse());

        transactionService.updateTransaction(transactionId,
                new TransactionRequestDTO(userId, "expense", new BigDecimal("55.50"), "Mercado"), CALLER);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals(TransactionType.EXPENSE, saved.getTransactionType());
        assertEquals(new BigDecimal("55.50"), saved.getAmount());
        assertEquals("Mercado", saved.getDescription());
    }

    @Test
    @DisplayName("updateTransaction throws when the transaction does not exist")
    void updateTransactionNotFound() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.updateTransaction(transactionId,
                new TransactionRequestDTO(userId, "INCOME", new BigDecimal("10.00"), "x"), CALLER));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTransaction rejects an unknown transaction type without touching the database")
    void updateTransactionInvalidType() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true))
                .thenReturn(Optional.of(sampleTransaction()));

        assertThrows(InvalidTransactionTypeException.class, () -> transactionService.updateTransaction(transactionId,
                new TransactionRequestDTO(userId, "TRANSFER", new BigDecimal("10.00"), "x"), CALLER));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTransaction performs a soft delete (recordStatus = false)")
    void deleteTransaction() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true))
                .thenReturn(Optional.of(sampleTransaction()));

        transactionService.deleteTransaction(transactionId, CALLER);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertFalse(captor.getValue().isRecordStatus());
    }

    @Test
    @DisplayName("deleteTransaction throws when the transaction does not exist")
    void deleteTransactionNotFound() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.deleteTransaction(transactionId, CALLER));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteByUserId issues the bulk soft delete with an updatedAt timestamp")
    void deleteByUserId() {
        transactionService.deleteByUserId(userId);

        verify(transactionRepository).deleteTransactionsByUserId(eq(userId), any(LocalDateTime.class));
    }

    // --- autorização por dono ---

    private final Caller OTHER = new Caller(UUID.randomUUID(), false);
    private final Caller ADMIN = new Caller(UUID.randomUUID(), true);

    @Test
    @DisplayName("deleteTransaction de outro dono -> AccessDeniedException, sem save")
    void deleteTransactionNotOwner() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true))
                .thenReturn(Optional.of(sampleTransaction()));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> transactionService.deleteTransaction(transactionId, OTHER));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("ADMIN deleta transação de qualquer usuário")
    void deleteTransactionAsAdmin() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true))
                .thenReturn(Optional.of(sampleTransaction()));

        transactionService.deleteTransaction(transactionId, ADMIN);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("getTransactionById de outro dono -> AccessDeniedException")
    void getTransactionByIdNotOwner() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true))
                .thenReturn(Optional.of(sampleTransaction()));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> transactionService.getTransactionById(transactionId, OTHER));
    }

    @Test
    @DisplayName("getTransactionsByUser de outro usuário -> AccessDeniedException")
    void getTransactionsByUserNotOwner() {
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> transactionService.getTransactionsByUser(userId, null, null, 0, 10, OTHER));
    }

    @Test
    @DisplayName("updateTransaction de outro dono -> AccessDeniedException, sem save")
    void updateTransactionNotOwner() {
        when(transactionRepository.findByIdAndRecordStatus(transactionId, true))
                .thenReturn(Optional.of(sampleTransaction()));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> transactionService.updateTransaction(transactionId,
                        new TransactionRequestDTO(userId, "INCOME", new BigDecimal("1.00"), "x"), OTHER));
        verify(transactionRepository, never()).save(any());
    }
}
