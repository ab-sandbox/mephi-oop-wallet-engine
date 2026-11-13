package ru.mephi.promoit.wallet.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.cli.executor.CommandExecutor;
import ru.mephi.promoit.wallet.cli.parser.CommandParser;
import ru.mephi.promoit.wallet.domain.model.Budget;
import ru.mephi.promoit.wallet.domain.model.Category;
import ru.mephi.promoit.wallet.domain.model.CategoryType;
import ru.mephi.promoit.wallet.domain.model.UserFinance;
import ru.mephi.promoit.wallet.domain.service.AuthService;
import ru.mephi.promoit.wallet.domain.service.BudgetService;
import ru.mephi.promoit.wallet.domain.service.TransferService;
import ru.mephi.promoit.wallet.domain.service.UserFinanceService;
import ru.mephi.promoit.wallet.persistence.CsvExportService;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryRenameIntegrationTest {

    private CommandParser parser;
    private CommandExecutor executor;
    private UserFinanceService userFinanceService;

    @BeforeEach
    void setUp() {
        UserRepository users = new UserRepository();
        UserFinanceRepository finances = new UserFinanceRepository();

        AuthService authService = new AuthService(
            users,
            new PasswordHasher()
        );

        userFinanceService = new UserFinanceService(
            authService,
            finances
        );

        TransferService transferService = new TransferService(
            authService,
            users,
            finances
        );

        parser = new CommandParser();

        executor = new CommandExecutor(
            authService,
            userFinanceService,
            transferService,
            new CsvExportService()
        );

        execute("register alex password123");
        execute("login alex password123");
    }

    @Test
    void shouldPreserveTransactionsAndBudgetAfterCategoryRename() {
        execute("category add income Salary");
        execute("category add expense Food");
        execute("income add Salary 10000");
        execute("budget set Food 4000");
        execute("expense add Food 1500");

        UserFinance finance = userFinanceService.current();

        Category original = finance.categories().findByName(
            "Food",
            CategoryType.EXPENSE
        );

        UUID originalId = original.id();

        String result = execute(
            "category rename expense Food Groceries"
        );

        assertEquals(
            "Category renamed: Food -> Groceries",
            result
        );

        Category renamed = finance.categories().findByName(
            "Groceries",
            CategoryType.EXPENSE
        );

        assertEquals(originalId, renamed.id());

        assertThrows(
            IllegalArgumentException.class,
            () -> finance.categories().findByName(
                "Food",
                CategoryType.EXPENSE
            )
        );

        // Existing transaction still references the same category.
        assertTrue(
            finance.wallet().getTransactions().stream()
                .anyMatch(transaction ->
                    transaction.categoryId().equals(originalId)
                    && transaction.amount().compareTo(
                        new BigDecimal("1500")
                    ) == 0
                )
        );

        // Budget still references the same category.
        Budget budget = finance.budgets().findByCategory(originalId);

        assertNotNull(budget);
        assertEquals(0, budget.limit().compareTo(
            new BigDecimal("4000")
        ));

        // New expenses use the renamed category.
        String expenseResult = execute(
            "expense add Groceries 1700"
        );

        assertTrue(expenseResult.contains(
            "Warning: 80% or more of Groceries budget used"
        ));

        BudgetService budgetService = new BudgetService(
            finance.budgets(),
            finance.categories(),
            finance.wallet()
        );

        assertEquals(
            0,
            new BigDecimal("3200").compareTo(
                budgetService.getSpent(budget)
            )
        );

        assertEquals(
            0,
            new BigDecimal("800").compareTo(
                budgetService.getRemaining(budget)
            )
        );

        // Reports and balance reflect both expenses.
        String report = execute("report expense Groceries");

        assertTrue(report.contains("Groceries"));
        assertTrue(report.contains("3200"));
        assertEquals("Current balance: 6800", execute("balance"));
    }

    private String execute(String input) {
        return executor.execute(parser.parse(input));
    }
}
