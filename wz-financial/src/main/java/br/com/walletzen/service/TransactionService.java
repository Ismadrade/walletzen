package br.com.walletzen.service;

import br.com.walletzen.client.UserValidationGateway;
import br.com.walletzen.domain.Transaction;
import br.com.walletzen.dto.request.TransactionRequestDTO;
import br.com.walletzen.dto.response.PageResponseDTO;
import br.com.walletzen.dto.response.TransactionResponseDTO;
import br.com.walletzen.enums.TransactionType;
import br.com.walletzen.exception.InvalidFilterException;
import br.com.walletzen.exception.TransactionNotFoundException;
import br.com.walletzen.mapper.TransactionMapper;
import br.com.walletzen.repository.TransactionRepository;
import br.com.walletzen.security.Caller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    static final int DEFAULT_PAGE_SIZE = 10;
    static final int MAX_PAGE_SIZE = 100;

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserValidationGateway userValidationGateway;

    /**
     * Lista as transações ativas do usuário, paginadas e opcionalmente filtradas
     * por período de criação (ano, ou ano + mês).
     *
     * @param year  ano (obrigatório quando {@code month} é informado)
     * @param month mês 1-12 (opcional; exige {@code year})
     * @param page  página, base 0
     * @param size  itens por página (limitado a {@value #MAX_PAGE_SIZE})
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<TransactionResponseDTO> getTransactionsByUser(UUID userId, Integer year, Integer month,
                                                                        int page, int size, Caller caller) {
        assertOwnerOrAdmin(userId, caller);
        Pageable pageable = buildPageable(page, size);
        DateRange range = resolveRange(year, month);

        Page<Transaction> result =
                transactionRepository.findActiveByUser(userId, range.start(), range.end(), pageable);

        return PageResponseDTO.from(result.map(transactionMapper::toResponse));
    }

    private Pageable buildPageable(int page, int size) {
        if (page < 0) {
            throw new InvalidFilterException("page must be zero or greater");
        }
        if (size < 1) {
            throw new InvalidFilterException("size must be greater than zero");
        }
        return PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private DateRange resolveRange(Integer year, Integer month) {
        if (year == null && month == null) {
            return DateRange.UNBOUNDED;
        }
        if (year == null) {
            throw new InvalidFilterException("month filter requires year");
        }
        if (month != null && (month < 1 || month > 12)) {
            throw new InvalidFilterException("month must be between 1 and 12");
        }

        try {
            LocalDateTime start = (month == null)
                    ? LocalDate.of(year, 1, 1).atStartOfDay()
                    : LocalDate.of(year, month, 1).atStartOfDay();
            LocalDateTime end = (month == null) ? start.plusYears(1) : start.plusMonths(1);
            return new DateRange(start, end);
        } catch (DateTimeException ex) {
            throw new InvalidFilterException("invalid year/month: " + ex.getMessage());
        }
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
        private static final DateRange UNBOUNDED = new DateRange(null, null);
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(UUID id, Caller caller) {
        Transaction transaction = transactionRepository.findByIdAndRecordStatus(id, true)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        assertOwnerOrAdmin(transaction.getUserId(), caller);
        return transactionMapper.toResponse(transaction);
    }

    /**
     * O dono do lançamento é sempre quem está autenticado — o {@code userId} do body é
     * ignorado para um {@code USER} comum. Só um {@code ADMIN} pode usar o {@code userId}
     * do body para lançar em nome de outra pessoa; sem ele, cai no próprio {@code ADMIN}
     * (que normalmente não tem lançamentos, então precisa informar).
     *
     * <p>Antes de gravar, o dono resolvido é validado sincronamente em {@code wz-user}
     * ({@link UserValidationGateway}): inexistente/inativo ⇒ 422; {@code wz-user} fora do ar ⇒ 503.
     */
    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO dto, Caller caller) {
        UUID ownerId = (caller.admin() && dto.userId() != null) ? dto.userId() : caller.wzUserId();
        if (ownerId == null) {
            throw new IllegalArgumentException("cannot determine the transaction owner");
        }

        userValidationGateway.assertActiveUser(ownerId);

        Transaction transaction = transactionMapper.toEntity(dto);
        transaction.setUserId(ownerId);
        transaction.setRecordStatus(true);
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponseDTO updateTransaction(UUID id, TransactionRequestDTO dto, Caller caller) {
        Transaction transaction = transactionRepository.findByIdAndRecordStatus(id, true)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        assertOwnerOrAdmin(transaction.getUserId(), caller);

        transaction.setTransactionType(TransactionType.fromString(dto.transactionType()));
        transaction.setAmount(dto.amount());
        transaction.setDescription(dto.description());
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void deleteTransaction(UUID id, Caller caller) {
        Transaction transaction = transactionRepository.findByIdAndRecordStatus(id, true)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        assertOwnerOrAdmin(transaction.getUserId(), caller);
        transaction.setRecordStatus(false);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        transactionRepository.deleteTransactionsByUserId(userId, LocalDateTime.now());
        log.info("Transactions has been deleted for userId {}", userId);
    }

    /** Dono da transação ou ADMIN; senão 403. */
    private void assertOwnerOrAdmin(UUID resourceOwnerId, Caller caller) {
        if (!caller.owns(resourceOwnerId)) {
            throw new AccessDeniedException("caller is not the owner of this resource");
        }
    }
}
