package ru.mephi.promoit.wallet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.model.Wallet;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceTest {

    private FinanceService service;

    @BeforeEach
    void setUp() {
        service = new FinanceService(new Wallet());
    }

    @Test
    void shouldAddIncomeAndUpdateBalance() {
        service.addIncome("Salary", new BigDecimal("50000.00"));

        assertEquals(
            new BigDecimal("50000.00"),
            service.getBalance()
        );
    }

    @Test
    void shouldRejectNegativeIncome() {
        assertThrows(IllegalArgumentException.class, () ->
            service.addIncome("Salary", new BigDecimal("-100.00"))
        );
    }

    @Test
    void shouldRejectBlankCategory() {
        assertThrows(IllegalArgumentException.class, () ->
            service.addIncome(" ", new BigDecimal("100.00"))
        );
    }
}
