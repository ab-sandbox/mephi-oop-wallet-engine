package ru.mephi.promoit.wallet.repository;

import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.domain.model.UserFinance;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserFinanceRepositoryTest {

    @Test
    void shouldRestoreUserFinance() {
        UserFinanceRepository repository = new UserFinanceRepository();

        UUID userId = UUID.randomUUID();
        UserFinance finance = new UserFinance();

        repository.restore(userId, finance);

        assertSame(finance, repository.getOrCreate(userId));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void shouldRejectDuplicateUserFinance() {
        UserFinanceRepository repository = new UserFinanceRepository();

        UUID userId = UUID.randomUUID();

        repository.restore(userId, new UserFinance());

        assertThrows(
            IllegalArgumentException.class,
            () -> repository.restore(userId, new UserFinance())
        );
    }

    @Test
    void shouldReturnIndependentMap() {
        UserFinanceRepository repository = new UserFinanceRepository();

        UUID userId = UUID.randomUUID();
        repository.getOrCreate(userId);

        assertThrows(
            UnsupportedOperationException.class,
            () -> repository.findAll().clear()
        );

        assertEquals(1, repository.findAll().size());
    }
}
