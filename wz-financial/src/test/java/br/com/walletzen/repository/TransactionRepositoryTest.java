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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private TestEntityManager em;

    private final UUID userId = UUID.randomUUID();

    private void persist(LocalDateTime createdAt, boolean active) {
        Transaction t = Transaction.builder()
                .transactionType(TransactionType.INCOME)
                .amount(new BigDecimal("10.00"))
                .description("x")
                .userId(userId)
                .build();
        em.persistAndFlush(t);
        // @PrePersist forces createdAt = now() and recordStatus = true; overwrite
        // both with the values this test actually needs to exercise the window.
        em.getEntityManager()
                .createNativeQuery("UPDATE WZ_TRANSACTION SET CREATED_AT = :ts, RECORD_STATUS = :active WHERE ID = :id")
                .setParameter("ts", createdAt)
                .setParameter("active", active)
                .setParameter("id", t.getId())
                .executeUpdate();
        em.clear();
    }

    @BeforeEach
    void seed() {
        persist(LocalDateTime.of(2026, 8, 1, 0, 0), true);      // first instant of Aug 2026
        persist(LocalDateTime.of(2026, 8, 31, 23, 59), true);   // last day of Aug 2026
        persist(LocalDateTime.of(2026, 9, 1, 0, 0), true);      // first instant of Sep 2026
        persist(LocalDateTime.of(2026, 7, 15, 12, 0), true);    // July 2026
        persist(LocalDateTime.of(2026, 8, 10, 9, 0), false);    // inactive, inside Aug
    }

    @Test
    @DisplayName("no window returns every active transaction of the user")
    void unbounded() {
        Page<Transaction> page = repository.findActiveByUser(userId, null, null, PageRequest.of(0, 10));

        assertEquals(4, page.getTotalElements());
    }

    @Test
    @DisplayName("month window is inclusive of the first instant and exclusive of the next month")
    void monthWindow() {
        Page<Transaction> page = repository.findActiveByUser(userId,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0),
                PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("year window covers the whole calendar year")
    void yearWindow() {
        Page<Transaction> page = repository.findActiveByUser(userId,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2027, 1, 1, 0, 0),
                PageRequest.of(0, 10));

        assertEquals(4, page.getTotalElements());
    }

    @Test
    @DisplayName("open lower bound only")
    void endOnly() {
        Page<Transaction> page = repository.findActiveByUser(userId, null,
                LocalDateTime.of(2026, 8, 1, 0, 0), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
    }
}
