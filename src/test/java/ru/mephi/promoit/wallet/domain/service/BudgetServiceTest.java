package ru.mephi.promoit.wallet.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.domain.model.*;
import ru.mephi.promoit.wallet.repository.BudgetCatalog;
import ru.mephi.promoit.wallet.repository.CategoryCatalog;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {

    private FinanceService financeService;
    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet();
        CategoryCatalog categories = new CategoryCatalog();
        BudgetCatalog budgets = new BudgetCatalog();

        financeService = new FinanceService(wallet, categories);
        budgetService = new BudgetService(budgets, categories, wallet);

        financeService.addCategory("Food", CategoryType.EXPENSE);
    }

    @Test
    void shouldCalculateRemainingBudget() {
        budgetService.setBudget("Food", new BigDecimal("4000"));
        financeService.addExpense("Food", new BigDecimal("1500"));

        Budget budget = budgetService.getBudgets().getFirst();

        assertEquals(
            0,
            new BigDecimal("2500")
                .compareTo(budgetService.getRemaining(budget))
        );
    }

    @Test
    void shouldWarnWhenBudgetThresholdReached() {
        budgetService.setBudget("Food", new BigDecimal("4000"));
        financeService.addExpense("Food", new BigDecimal("3500"));

        String warning = budgetService.getWarning("Food");

        assertNotNull(warning);
        assertTrue(warning.contains("80%"));
    }

    @Test
    void shouldWarnWhenBudgetExceeded() {
        budgetService.setBudget("Food", new BigDecimal("4000"));
        financeService.addExpense("Food", new BigDecimal("4500"));

        String warning = budgetService.getWarning("Food");

        assertNotNull(warning);
        assertTrue(warning.contains("exceeded"));
        assertTrue(warning.contains("500"));
    }

    @Test
    void shouldRejectBudgetForUnknownCategory() {
        assertThrows(
            IllegalArgumentException.class,
            () -> budgetService.setBudget(
                "Unknown",
                new BigDecimal("1000")
            )
        );
    }

    @Test
    void shouldNotWarnWithoutBudget() {
        financeService.addExpense("Food", new BigDecimal("1500"));

        assertNull(budgetService.getWarning("Food"));
    }

    @Test
    void shouldNotIncludeOtherCategoryExpenses() {
        financeService.addCategory("Transport", CategoryType.EXPENSE);

        budgetService.setBudget("Food", new BigDecimal("4000"));

        financeService.addExpense("Food", new BigDecimal("1000"));
        financeService.addExpense("Transport", new BigDecimal("2000"));

        Budget budget = budgetService.getBudgets().getFirst();

        assertEquals(
            0,
            new BigDecimal("3000")
                .compareTo(budgetService.getRemaining(budget))
        );
    }

    @Test
    void shouldNotWarnBelowBudgetThreshold() {
        budgetService.setBudget("Food", new BigDecimal("4000"));
        financeService.addExpense("Food", new BigDecimal("3199"));

        assertNull(budgetService.getWarning("Food"));
    }

    @Test
    void shouldWarnAtExactBudgetThreshold() {
        budgetService.setBudget("Food", new BigDecimal("4000"));
        financeService.addExpense("Food", new BigDecimal("3200"));

        assertEquals(
            "Warning: 80% or more of Food budget used",
            budgetService.getWarning("Food")
        );
    }
}
