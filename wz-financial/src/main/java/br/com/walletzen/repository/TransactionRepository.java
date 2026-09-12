package br.com.walletzen.repository;

import br.com.walletzen.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdAndRecordStatus(UUID id, boolean recordStatus);

    /**
     * Active transactions of a user, optionally bounded by a {@code transactionDate}
     * window {@code [start, end)}. A null {@code start}/{@code end} drops that bound,
     * so passing both null returns every active transaction of the user.
     * <p>
     * The {@code CAST(:param AS date)} is required: without it PostgreSQL
     * cannot infer the type of the bind used only in {@code :param IS NULL} and
     * fails to prepare the statement ("could not determine data type of parameter").
     */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.userId = :userId
              AND t.recordStatus = true
              AND (CAST(:start AS date) IS NULL OR t.transactionDate >= :start)
              AND (CAST(:end AS date) IS NULL OR t.transactionDate < :end)
            """)
    Page<Transaction> findActiveByUser(@Param("userId") UUID userId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end,
                                       Pageable pageable);

    @Modifying
    @Query("UPDATE Transaction t SET t.recordStatus = false, t.updatedAt = :now WHERE t.userId = :userId")
    void deleteTransactionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
