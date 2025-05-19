package br.com.walletzen.repository;

import br.com.walletzen.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByUserIdAndRecordStatus(UUID userId, boolean recordStatus);
    Optional<Transaction> findByIdAndRecordStatus(UUID id, boolean recordStatus);

    @Modifying
    @Query("UPDATE Transaction t SET t.recordStatus = false WHERE t.userId = :userId")
    void deleteTransactionsByUserId(@Param("userId") UUID userId);
}
