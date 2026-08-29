package br.com.walletzen.repository;

import br.com.walletzen.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserIdAndRecordStatus(UUID userId, boolean recordStatus, Pageable pageable);

    Optional<Transaction> findByIdAndRecordStatus(UUID id, boolean recordStatus);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.userId = :userId
              AND t.recordStatus = true
              AND t.createdAt >= :start
              AND t.createdAt < :end
            """)
    Page<Transaction> findActiveByUserAndCreatedAtBetween(@Param("userId") UUID userId,
                                                          @Param("start") LocalDateTime start,
                                                          @Param("end") LocalDateTime end,
                                                          Pageable pageable);

    @Modifying
    @Query("UPDATE Transaction t SET t.recordStatus = false, t.updatedAt = :now WHERE t.userId = :userId")
    void deleteTransactionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
