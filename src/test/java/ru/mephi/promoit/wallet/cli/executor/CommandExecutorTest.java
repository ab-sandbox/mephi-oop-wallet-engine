package ru.mephi.promoit.wallet.cli.executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.cli.parser.CommandParser;
import ru.mephi.promoit.wallet.domain.service.AuthService;
import ru.mephi.promoit.wallet.domain.service.TransferService;
import ru.mephi.promoit.wallet.domain.service.UserFinanceService;
import ru.mephi.promoit.wallet.persistence.CsvExportService;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

import static org.junit.jupiter.api.Assertions.*;

class CommandExecutorTest {

    private CommandParser parser;
    private CommandExecutor executor;

    @BeforeEach
    void setUp() {
        UserRepository users = new UserRepository();
        UserFinanceRepository finances = new UserFinanceRepository();

        AuthService authService = new AuthService(
            users,
            new PasswordHasher()
        );

        UserFinanceService userFinanceService = new UserFinanceService(
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
        execute("category add income Salary");
        execute("category add expense Food");
    }

    @Test
    void shouldWarnWhenBalanceBecomesZero() {
        execute("income add Salary 5000");

        String result = execute("expense add Food 5000");

        assertTrue(result.contains("Warning: Balance is zero!"));
        assertEquals("Current balance: 0", execute("balance"));
    }

    @Test
    void shouldWarnWhenExpensesExceedIncome() {
        String result = execute("expense add Food 1000");

        assertTrue(
            result.contains("Warning: Total expenses exceed total income!")
        );
        assertEquals("Current balance: -1000", execute("balance"));
    }

    @Test
    void shouldWarnAboutBudgetAndZeroBalance() {
        execute("income add Salary 5000");
        execute("budget set Food 4000");

        String result = execute("expense add Food 5000");

        assertTrue(result.contains("Food budget exceeded by 1000"));
        assertTrue(result.contains("Warning: Balance is zero!"));
    }

    private String execute(String input) {
        return executor.execute(parser.parse(input));
    }
}
