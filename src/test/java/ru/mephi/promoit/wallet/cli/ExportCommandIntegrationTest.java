package ru.mephi.promoit.wallet.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.mephi.promoit.wallet.cli.executor.CommandExecutor;
import ru.mephi.promoit.wallet.cli.parser.CommandParser;
import ru.mephi.promoit.wallet.domain.service.AuthService;
import ru.mephi.promoit.wallet.domain.service.TransferService;
import ru.mephi.promoit.wallet.domain.service.UserFinanceService;
import ru.mephi.promoit.wallet.persistence.CsvExportService;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportCommandIntegrationTest {

    @TempDir
    Path tempDir;

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
    }

    @Test
    void shouldExportTransactionsThroughCli() throws Exception {
        execute("register alex password123");
        execute("login alex password123");
        execute("category add income Salary");
        execute("category add expense Food");
        execute("income add Salary 5000");
        execute("expense add Food 300");

        Path file = tempDir.resolve("transactions.csv");

        String result = execute("export csv " + file);

        assertEquals(
            "Exported 2 transactions to " + file,
            result
        );

        List<String> lines = Files.readAllLines(file);

        assertEquals(3, lines.size());
        assertEquals(
            "id,type,category,amount,createdAt,transferId",
            lines.getFirst()
        );
        assertTrue(lines.get(1).contains(",INCOME,Salary,5000,"));
        assertTrue(lines.get(2).contains(",EXPENSE,Food,300,"));
    }

    @Test
    void shouldExportOnlyCurrentUserTransactions() throws Exception {
        execute("register alex password123");
        execute("register bob password123");

        execute("login alex password123");
        execute("category add income Salary");
        execute("income add Salary 5000");

        execute("logout");
        execute("login bob password123");
        execute("category add income Bonus");
        execute("income add Bonus 1000");

        Path file = tempDir.resolve("bob.csv");

        execute("export csv " + file);

        String csv = Files.readString(file);

        assertTrue(csv.contains(",INCOME,Bonus,1000,"));
        assertFalse(csv.contains("Salary"));
        assertFalse(csv.contains("5000"));
    }

    @Test
    void shouldRejectExportWithoutAuthentication() {
        Path file = tempDir.resolve("unauthorized.csv");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> execute("export csv " + file)
        );

        assertEquals(
            "Authentication required",
            exception.getMessage()
        );

        assertFalse(Files.exists(file));
    }

    @Test
    void shouldRejectUnsupportedExportFormat() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> execute("export xml transactions.xml")
        );

        assertEquals(
            "Unsupported export format: xml",
            exception.getMessage()
        );
    }

    @Test
    void shouldHandleFileWriteError() {
        execute("register alex password123");
        execute("login alex password123");

        Path file = tempDir.resolve("missing").resolve("data.csv");

        String result = execute("export csv " + file);

        assertTrue(
            result.startsWith("Failed to export transactions:")
        );

        assertFalse(Files.exists(file));
    }

    private String execute(String input) {
        return executor.execute(parser.parse(input));
    }
}
