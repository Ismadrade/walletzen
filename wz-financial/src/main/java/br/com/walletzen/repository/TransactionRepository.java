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

    Optional<Transaction> findByIdAndRecordStatus(UUID id, boolean recordStatus);

    /**
     * Active transactions of a user, optionally bounded by a {@code createdAt}
     * window. A null {@code start}/{@code end} drops that bound, so passing both
     * null returns every active transaction of the user.
     */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.userId = :userId
              AND t.recordStatus = true
              AND (:start IS NULL OR t.createdAt >= :start)
              AND (:end IS NULL OR t.createdAt < :end)
            """)
    Page<Transaction> findActiveByUser(@Param("userId") UUID userId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       Pageable pageable);

    @Modifying
    @Query("UPDATE Transaction t SET t.recordStatus = false, t.updatedAt = :now WHERE t.userId = :userId")
    void deleteTransactionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
