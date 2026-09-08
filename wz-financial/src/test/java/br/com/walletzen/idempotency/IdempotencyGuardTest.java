package br.com.walletzen.idempotency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class IdempotencyGuardTest {

    @Autowired
    private ProcessedEventRepository repository;

    private IdempotencyGuard guard;

    @BeforeEach
    void setUp() {
        guard = new IdempotencyGuard(repository);
    }

    @Test
    @DisplayName("firstTime: true na primeira vez, false depois (mesmo eventId + consumidor)")
    void firstThenDuplicate() {
        UUID eventId = UUID.randomUUID();

        assertTrue(guard.firstTime(eventId, "c1"));
        assertFalse(guard.firstTime(eventId, "c1"));
        assertFalse(guard.firstTime(eventId, "c1"));
    }

    @Test
    @DisplayName("A marca é por consumidor: o mesmo eventId é 'novo' para outro consumidor")
    void perConsumer() {
        UUID eventId = UUID.randomUUID();

        assertTrue(guard.firstTime(eventId, "c1"));
        assertTrue(guard.firstTime(eventId, "c2"));
        assertFalse(guard.firstTime(eventId, "c2"));
    }
}
