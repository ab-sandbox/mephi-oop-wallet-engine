package ru.mephi.promoit.wallet.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.cli.executor.CommandExecutor;
import ru.mephi.promoit.wallet.cli.parser.CommandParser;
import ru.mephi.promoit.wallet.domain.model.Transaction;
import ru.mephi.promoit.wallet.domain.model.User;
import ru.mephi.promoit.wallet.domain.service.AuthService;
import ru.mephi.promoit.wallet.domain.service.TransferService;
import ru.mephi.promoit.wallet.domain.service.UserFinanceService;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferCommandIntegrationTest {

    private CommandParser parser;
    private CommandExecutor executor;
    private UserFinanceRepository finances;
    private UserRepository users;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        finances = new UserFinanceRepository();

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
            null
        );
    }

    @Test
    void shouldTransferMoneyThroughCli() {
        execute("register alex password123");
        execute("register bob password123");

        execute("login alex password123");
        execute("category add income Salary");
        execute("income add Salary 5000");

        assertEquals(
            "Transferred 1500 to bob",
            execute("transfer bob 1500")
        );

        assertEquals("Current balance: 3500", execute("balance"));

        execute("logout");
        execute("login bob password123");

        assertEquals("Current balance: 1500", execute("balance"));

        User alex = users.findByLogin("alex").orElseThrow();
        User bob = users.findByLogin("bob").orElseThrow();

        Transaction expense = finances.getOrCreate(alex.id())
            .wallet()
            .getTransactions()
            .stream()
            .filter(transaction -> transaction.transferId() != null)
            .findFirst()
            .orElseThrow();

        Transaction income = finances.getOrCreate(bob.id())
            .wallet()
            .getTransactions()
            .getFirst();

        assertNotNull(expense.transferId());
        assertEquals(expense.transferId(), income.transferId());
        assertEquals(
            new BigDecimal("1500"),
            income.amount()
        );
    }

    @Test
    void shouldRejectTransferWithoutAuthentication() {
        execute("register bob password123");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> execute("transfer bob 100")
        );

        assertEquals(
            "Authentication required",
            exception.getMessage()
        );
    }

    @Test
    void shouldRejectInvalidTransferAmount() {
        execute("register alex password123");
        execute("register bob password123");
        execute("login alex password123");

        assertThrows(
            IllegalArgumentException.class,
            () -> execute("transfer bob invalid")
        );
    }

    private String execute(String input) {
        return executor.execute(parser.parse(input));
    }
}
