package br.com.walletzen.service;

import br.com.walletzen.domain.Transaction;
import br.com.walletzen.dto.request.TransactionRequestDTO;
import br.com.walletzen.dto.response.TransactionResponseDTO;
import br.com.walletzen.enums.TransactionType;
import br.com.walletzen.exception.TransactionNotFoundException;
import br.com.walletzen.mapper.TransactionMapper;
import br.com.walletzen.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public List<TransactionResponseDTO> getTransactionsByUser(UUID userId) {
        return transactionRepository.findByUserIdAndRecordStatus(userId, true)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

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

        transaction.setTransactionType(TransactionType.valueOf(dto.transactionType()));
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
}
