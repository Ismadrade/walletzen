package br.com.walletzen.service;

import br.com.walletzen.domain.Transaction;
import br.com.walletzen.dto.request.TransactionRequestDTO;
import br.com.walletzen.dto.response.PageResponseDTO;
import br.com.walletzen.dto.response.TransactionResponseDTO;
import br.com.walletzen.enums.TransactionType;
import br.com.walletzen.exception.InvalidFilterException;
import br.com.walletzen.exception.TransactionNotFoundException;
import br.com.walletzen.mapper.TransactionMapper;
import br.com.walletzen.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                                                                        int page, int size) {
        Pageable pageable = buildPageable(page, size);

        Page<Transaction> result = (year == null && month == null)
                ? transactionRepository.findByUserIdAndRecordStatus(userId, true, pageable)
                : findByPeriod(userId, year, month, pageable);

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

    private Page<Transaction> findByPeriod(UUID userId, Integer year, Integer month, Pageable pageable) {
        if (year == null) {
            throw new InvalidFilterException("month filter requires year");
        }
        if (month != null && (month < 1 || month > 12)) {
            throw new InvalidFilterException("month must be between 1 and 12");
        }

        LocalDateTime start;
        LocalDateTime end;
        try {
            if (month == null) {
                start = LocalDate.of(year, 1, 1).atStartOfDay();
                end = start.plusYears(1);
            } else {
                start = LocalDate.of(year, month, 1).atStartOfDay();
                end = start.plusMonths(1);
            }
        } catch (DateTimeException ex) {
            throw new InvalidFilterException("invalid year/month: " + ex.getMessage());
        }

        return transactionRepository.findActiveByUserAndCreatedAtBetween(userId, start, end, pageable);
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(UUID id) {
        Transaction transaction = transactionRepository.findByIdAndRecordStatus(id, true)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO dto) {
        Transaction transaction = transactionMapper.toEntity(dto);
        transaction.setRecordStatus(true);
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponseDTO updateTransaction(UUID id, TransactionRequestDTO dto) {
        Transaction transaction = transactionRepository.findByIdAndRecordStatus(id, true)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        transaction.setTransactionType(TransactionType.fromString(dto.transactionType()));
        transaction.setAmount(dto.amount());
        transaction.setDescription(dto.description());
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction transaction = transactionRepository.findByIdAndRecordStatus(id, true)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        transaction.setRecordStatus(false);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        transactionRepository.deleteTransactionsByUserId(userId, LocalDateTime.now());
        log.info("Transactions has been deleted for userId {}", userId);
    }
}
