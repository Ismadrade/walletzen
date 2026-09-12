package br.com.walletzen.repository;

import br.com.walletzen.domain.Transaction;
import br.com.walletzen.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private TestEntityManager em;

    private final UUID userId = UUID.randomUUID();

    private Transaction build(LocalDate transactionDate) {
        return Transaction.builder()
                .transactionType(TransactionType.INCOME)
                .amount(new BigDecimal("10.00"))
                .description("x")
                .userId(userId)
                .transactionDate(transactionDate)
                .build();
    }

    private void persist(LocalDate transactionDate, boolean active) {
        Transaction t = build(transactionDate);
        em.persistAndFlush(t);
        // @PrePersist forces recordStatus = true; overwrite it for the inactive case.
        em.getEntityManager()
                .createNativeQuery("UPDATE WZ_TRANSACTION SET RECORD_STATUS = :active WHERE ID = :id")
                .setParameter("active", active)
                .setParameter("id", t.getId())
                .executeUpdate();
        em.clear();
    }

    @BeforeEach
    void seed() {
        persist(LocalDate.of(2026, 8, 1), true);     // first day of Aug 2026
        persist(LocalDate.of(2026, 8, 31), true);    // last day of Aug 2026
        persist(LocalDate.of(2026, 9, 1), true);     // first day of Sep 2026
        persist(LocalDate.of(2026, 7, 15), true);    // July 2026
        persist(LocalDate.of(2026, 8, 10), false);   // inactive, inside Aug
    }

    @Test
    @DisplayName("no window returns every active transaction of the user")
    void unbounded() {
        Page<Transaction> page = repository.findActiveByUser(userId, null, null, PageRequest.of(0, 10));

        assertEquals(4, page.getTotalElements());
    }

    @Test
    @DisplayName("month window is inclusive of the first day and exclusive of the next month")
    void monthWindow() {
        Page<Transaction> page = repository.findActiveByUser(userId,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 9, 1),
                PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("year window covers the whole calendar year")
    void yearWindow() {
        Page<Transaction> page = repository.findActiveByUser(userId,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                PageRequest.of(0, 10));

        assertEquals(4, page.getTotalElements());
    }

    @Test
    @DisplayName("open lower bound only")
    void endOnly() {
        Page<Transaction> page = repository.findActiveByUser(userId, null,
                LocalDate.of(2026, 8, 1), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
    }

    @Test
    @DisplayName("a transaction saved without a date gets today's date")
    void defaultsTransactionDateToToday() {
        Transaction t = build(null);
        em.persistAndFlush(t);

        assertEquals(LocalDate.now(), t.getTransactionDate());
    }
}
