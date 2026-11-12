package ru.mephi.promoit.wallet.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.domain.model.*;
import ru.mephi.promoit.wallet.repository.CategoryCatalog;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsServiceTest {

    private FinanceService financeService;
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet();
        CategoryCatalog categories = new CategoryCatalog();

        financeService = new FinanceService(wallet, categories);
        statisticsService = new StatisticsService(wallet, categories);

        financeService.addCategory("Salary", CategoryType.INCOME);
        financeService.addCategory("Food", CategoryType.EXPENSE);
        financeService.addCategory("Transport", CategoryType.EXPENSE);
    }

    @Test
    void shouldCalculateSummary() {
        financeService.addIncome("Salary", new BigDecimal("50000"));
        financeService.addExpense("Food", new BigDecimal("3000"));
        financeService.addExpense("Transport", new BigDecimal("1500"));

        FinanceSummary summary = statisticsService.getSummary();

        assertEquals(0, new BigDecimal("50000").compareTo(summary.income()));
        assertEquals(0, new BigDecimal("4500").compareTo(summary.expenses()));
        assertEquals(0, new BigDecimal("45500").compareTo(summary.balance()));
    }

    @Test
    void shouldReturnZeroSummaryForEmptyWallet() {
        FinanceSummary summary = statisticsService.getSummary();

        assertEquals(0, BigDecimal.ZERO.compareTo(summary.income()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.expenses()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.balance()));
    }

    @Test
    void shouldGroupExpensesByCategory() {
        financeService.addExpense("Food", new BigDecimal("1000"));
        financeService.addExpense("Food", new BigDecimal("2500"));
        financeService.addExpense("Transport", new BigDecimal("1500"));

        List<CategoryReport> report = statisticsService.getCategoryReport(
            TransactionType.EXPENSE
        );

        assertEquals(2, report.size());

        assertEquals("Food", report.get(0).category());
        assertEquals(
            0,
            new BigDecimal("3500").compareTo(report.get(0).amount())
        );

        assertEquals("Transport", report.get(1).category());
        assertEquals(
            0,
            new BigDecimal("1500").compareTo(report.get(1).amount())
        );
    }

    @Test
    void shouldExcludeOtherTransactionTypes() {
        financeService.addIncome("Salary", new BigDecimal("50000"));
        financeService.addExpense("Food", new BigDecimal("1000"));

        List<CategoryReport> report = statisticsService.getCategoryReport(
            TransactionType.INCOME
        );

        assertEquals(1, report.size());
        assertEquals("Salary", report.getFirst().category());
        assertEquals(
            0,
            new BigDecimal("50000").compareTo(report.getFirst().amount())
        );
    }
}
