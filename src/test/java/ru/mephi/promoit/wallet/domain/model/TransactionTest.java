package ru.mephi.promoit.wallet.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void shouldCreateValidTransaction() {
        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            TransactionType.EXPENSE,
            UUID.randomUUID(),
            new BigDecimal("500.00"),
            Instant.parse("2026-11-06T10:00:00Z"),
            null
        );

        assertEquals(TransactionType.EXPENSE, transaction.type());
        assertEquals(new BigDecimal("500.00"), transaction.amount());
        assertNull(transaction.transferId());
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(
                UUID.randomUUID(),
                TransactionType.EXPENSE,
                UUID.randomUUID(),
                new BigDecimal("-100.00"),
                Instant.now(),
                null
            )
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(
                UUID.randomUUID(),
                TransactionType.INCOME,
                UUID.randomUUID(),
                BigDecimal.ZERO,
                Instant.now(),
                null
            )
        );
    }
}
