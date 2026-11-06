package ru.mephi.promoit.wallet.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BudgetTest {

    @Test
    void shouldCalculateRemainingBudget() {
        Budget budget = new Budget(
            UUID.randomUUID(),
            new BigDecimal("4000.00")
        );

        BigDecimal remaining = budget.remaining(
            new BigDecimal("800.00")
        );

        assertEquals(new BigDecimal("3200.00"), remaining);
    }

    @Test
    void shouldDetectExceededBudget() {
        Budget budget = new Budget(
            UUID.randomUUID(),
            new BigDecimal("2500.00")
        );

        assertTrue(budget.isExceeded(new BigDecimal("3000.00")));
    }

    @Test
    void shouldNotTreatExactLimitAsExceeded() {
        Budget budget = new Budget(
            UUID.randomUUID(),
            new BigDecimal("3000.00")
        );

        assertFalse(budget.isExceeded(new BigDecimal("3000.00")));
    }

    @Test
    void shouldRejectNegativeLimit() {
        assertThrows(IllegalArgumentException.class, () ->
            new Budget(
                UUID.randomUUID(),
                new BigDecimal("-100.00")
            )
        );
    }

    @Test
    void shouldRejectNegativeSpentAmount() {
        Budget budget = new Budget(
            UUID.randomUUID(),
            new BigDecimal("1000.00")
        );

        assertThrows(IllegalArgumentException.class, () ->
            budget.remaining(new BigDecimal("-100.00"))
        );
    }
}
