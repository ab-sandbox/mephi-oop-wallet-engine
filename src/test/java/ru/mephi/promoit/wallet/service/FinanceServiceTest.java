package ru.mephi.promoit.wallet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.model.CategoryCatalog;
import ru.mephi.promoit.wallet.model.CategoryType;
import ru.mephi.promoit.wallet.model.Wallet;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceTest {

    private FinanceService service;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet();
        CategoryCatalog categories = new CategoryCatalog();

        categories.add("Salary", CategoryType.INCOME);
        categories.add("Food", CategoryType.EXPENSE);

        service = new FinanceService(wallet, categories);
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

    @Test
    void shouldAddExpenseAndUpdateBalance() {
        service.addIncome("Salary", new BigDecimal("50000.00"));
        service.addExpense("Food", new BigDecimal("1500.00"));

        assertEquals(
            new BigDecimal("48500.00"),
            service.getBalance()
        );
    }

    @Test
    void shouldRejectUnknownCategory() {
        assertThrows(IllegalArgumentException.class, () ->
            service.addExpense("Transport", new BigDecimal("500.00"))
        );
    }

    @Test
    void shouldRejectWrongCategoryType() {
        assertThrows(IllegalArgumentException.class, () ->
            service.addExpense("Salary", new BigDecimal("500.00"))
        );
    }

    @Test
    void shouldRejectDuplicateCategory() {
        assertThrows(IllegalArgumentException.class, () ->
            service.addCategory("food", CategoryType.EXPENSE)
        );
    }

    @Test
    void shouldAllowSameNameForDifferentCategoryTypes() {
        assertDoesNotThrow(() ->
            service.addCategory("Food", CategoryType.INCOME)
        );
    }
}
