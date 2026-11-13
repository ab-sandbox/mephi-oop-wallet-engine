package ru.mephi.promoit.wallet.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.domain.model.CategoryReport;
import ru.mephi.promoit.wallet.domain.model.CategoryType;
import ru.mephi.promoit.wallet.domain.model.FinanceSummary;
import ru.mephi.promoit.wallet.domain.model.TransactionType;
import ru.mephi.promoit.wallet.domain.model.Wallet;
import ru.mephi.promoit.wallet.repository.CategoryCatalog;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void shouldReportSelectedCategories() {
        Wallet wallet = new Wallet();
        CategoryCatalog categories = new CategoryCatalog();
        FinanceService finance = new FinanceService(wallet, categories);

        finance.addCategory("Food", CategoryType.EXPENSE);
        finance.addCategory("Taxi", CategoryType.EXPENSE);
        finance.addCategory("Games", CategoryType.EXPENSE);

        finance.addExpense("Food", new BigDecimal("300"));
        finance.addExpense("Food", new BigDecimal("500"));
        finance.addExpense("Taxi", new BigDecimal("1500"));
        finance.addExpense("Games", new BigDecimal("3000"));

        StatisticsService statistics = new StatisticsService(wallet, categories);

        List<CategoryReport> report = statistics.getCategoryReport(
            TransactionType.EXPENSE,
            List.of("Food", "Taxi")
        );

        assertEquals(2, report.size());
        assertEquals("Food", report.get(0).category());
        assertEquals(new BigDecimal("800"), report.get(0).amount());
        assertEquals("Taxi", report.get(1).category());
        assertEquals(new BigDecimal("1500"), report.get(1).amount());

        BigDecimal total = report.stream()
            .map(CategoryReport::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("2300"), total);
    }

    @Test
    void shouldRejectUnknownCategoryInReport() {
        StatisticsService statistics = new StatisticsService(
            new Wallet(),
            new CategoryCatalog()
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> statistics.getCategoryReport(
                TransactionType.EXPENSE,
                List.of("Unknown")
            )
        );

        assertEquals("Category not found: Unknown", exception.getMessage());
    }

    @Test
    void shouldReportZeroForCategoryWithoutTransactions() {
        CategoryCatalog categories = new CategoryCatalog();
        categories.add("Food", CategoryType.EXPENSE);

        StatisticsService statistics = new StatisticsService(
            new Wallet(),
            categories
        );

        List<CategoryReport> report = statistics.getCategoryReport(
            TransactionType.EXPENSE,
            List.of("Food")
        );

        assertEquals(1, report.size());
        assertEquals("Food", report.getFirst().category());
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getFirst().amount()));
    }

    @Test
    void shouldNotDuplicateSelectedCategories() {
        CategoryCatalog categories = new CategoryCatalog();
        categories.add("Food", CategoryType.EXPENSE);

        StatisticsService statistics = new StatisticsService(
            new Wallet(),
            categories
        );

        List<CategoryReport> report = statistics.getCategoryReport(
            TransactionType.EXPENSE,
            List.of("Food", "Food")
        );

        assertEquals(1, report.size());
        assertEquals("Food", report.get(0).category());
    }
}
