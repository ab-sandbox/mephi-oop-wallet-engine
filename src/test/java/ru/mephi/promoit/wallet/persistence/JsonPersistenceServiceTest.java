package ru.mephi.promoit.wallet.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.mephi.promoit.wallet.domain.model.Budget;
import ru.mephi.promoit.wallet.domain.model.Category;
import ru.mephi.promoit.wallet.domain.model.CategoryType;
import ru.mephi.promoit.wallet.domain.model.SystemCategory;
import ru.mephi.promoit.wallet.domain.model.Transaction;
import ru.mephi.promoit.wallet.domain.model.TransactionType;
import ru.mephi.promoit.wallet.domain.model.User;
import ru.mephi.promoit.wallet.domain.model.UserFinance;
import ru.mephi.promoit.wallet.domain.service.FinanceService;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonPersistenceServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveAndRestoreApplicationState() throws Exception {
        UserRepository users = new UserRepository();
        UserFinanceRepository finances = new UserFinanceRepository();

        User alex = new User(
            UUID.randomUUID(),
            "alex",
            "stored-password-hash"
        );

        User bob = new User(
            UUID.randomUUID(),
            "bob",
            "another-password-hash"
        );

        users.save(alex);
        users.save(bob);

        UserFinance alexFinance = finances.getOrCreate(alex.id());

        FinanceService financeService = new FinanceService(
            alexFinance.wallet(),
            alexFinance.categories()
        );

        Category salary = financeService.addCategory(
            "Salary",
            CategoryType.INCOME
        );

        Category food = financeService.addCategory(
            "Food",
            CategoryType.EXPENSE
        );

        financeService.addIncome("Salary", new BigDecimal("50000"));
        financeService.addExpense("Food", new BigDecimal("3000"));

        alexFinance.budgets().set(
            new Budget(food.id(), new BigDecimal("10000"))
        );

        finances.getOrCreate(bob.id());

        Path file = tempDir.resolve("wallet-data.json");
        JsonPersistenceService persistence = new JsonPersistenceService(file);

        persistence.save(users, finances);

        LoadedState loaded = persistence.load();

        User restoredAlex = loaded.users()
            .findByLogin("alex")
            .orElseThrow();

        assertEquals(alex.id(), restoredAlex.id());
        assertEquals(alex.passwordHash(), restoredAlex.passwordHash());

        UserFinance restoredFinance = loaded.finances()
            .getOrCreate(alex.id());

        assertEquals(
            0,
            new BigDecimal("47000").compareTo(
                restoredFinance.wallet().getBalance()
            )
        );

        Category restoredSalary = restoredFinance.categories()
            .findByName("Salary", CategoryType.INCOME);

        assertEquals(salary.id(), restoredSalary.id());

        Budget restoredBudget = restoredFinance.budgets()
            .findByCategory(food.id());

        assertNotNull(restoredBudget);
        assertEquals(
            0,
            new BigDecimal("10000")
                .compareTo(restoredBudget.limit())
        );

        assertEquals(
            2,
            restoredFinance.wallet().getTransactions().size()
        );

        UserFinance restoredBob = loaded.finances()
            .getOrCreate(bob.id());

        assertEquals(
            0,
            BigDecimal.ZERO.compareTo(
                restoredBob.wallet().getBalance()
            )
        );
    }

    @Test
    void shouldReturnEmptyStateWhenFileDoesNotExist() throws Exception {
        JsonPersistenceService persistence = new JsonPersistenceService(
            tempDir.resolve("missing.json")
        );

        LoadedState loaded = persistence.load();

        assertTrue(loaded.users().findAll().isEmpty());
        assertTrue(loaded.finances().findAll().isEmpty());
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        Path file = tempDir.resolve("invalid.json");

        Files.writeString(file, "{ invalid json");

        JsonPersistenceService persistence = new JsonPersistenceService(file);

        assertThrows(IOException.class, persistence::load);

        assertEquals("{ invalid json", Files.readString(file));
    }

    @Test
    void shouldRejectTransactionWithUnknownCategory() throws Exception {
        UserRepository users = new UserRepository();
        UserFinanceRepository finances = new UserFinanceRepository();

        User user = new User(
            UUID.randomUUID(),
            "alex",
            "stored-hash"
        );

        users.save(user);

        UserFinance finance = finances.getOrCreate(user.id());

        finance.wallet().addTransaction(new Transaction(
            UUID.randomUUID(),
            TransactionType.EXPENSE,
            UUID.randomUUID(),
            new BigDecimal("100"),
            Instant.now(),
            null
        ));

        Path file = tempDir.resolve("invalid-reference.json");

        JsonPersistenceService persistence = new JsonPersistenceService(file);

        persistence.save(users, finances);

        assertThrows(IOException.class, persistence::load);
    }

    @Test
    void shouldPreserveTransferIdAfterReload() throws Exception {
        UserRepository users = new UserRepository();
        UserFinanceRepository finances = new UserFinanceRepository();

        User alex = new User(
            UUID.randomUUID(),
            "alex",
            "stored-hash"
        );

        User bob = new User(
            UUID.randomUUID(),
            "bob",
            "stored-hash"
        );

        users.save(alex);
        users.save(bob);

        UserFinance alexFinance = finances.getOrCreate(alex.id());
        UserFinance bobFinance = finances.getOrCreate(bob.id());

        Category salary = alexFinance.categories().add(
            "Salary",
            CategoryType.INCOME
        );

        Category transferOut = alexFinance.categories().add(
            SystemCategory.TRANSFER_OUT.categoryName(),
            SystemCategory.TRANSFER_OUT.categoryType()
        );

        Category transferIn = bobFinance.categories().add(
            SystemCategory.TRANSFER_IN.categoryName(),
            SystemCategory.TRANSFER_IN.categoryType()
        );

        alexFinance.wallet().addTransaction(new Transaction(
            UUID.randomUUID(),
            TransactionType.INCOME,
            salary.id(),
            new BigDecimal("5000"),
            Instant.now(),
            null
        ));

        UUID transferId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID incomeId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        alexFinance.wallet().addTransaction(new Transaction(
            expenseId,
            TransactionType.EXPENSE,
            transferOut.id(),
            new BigDecimal("1500"),
            createdAt,
            transferId
        ));

        bobFinance.wallet().addTransaction(new Transaction(
            incomeId,
            TransactionType.INCOME,
            transferIn.id(),
            new BigDecimal("1500"),
            createdAt,
            transferId
        ));

        JsonPersistenceService persistence = new JsonPersistenceService(
            tempDir.resolve("transfers.json")
        );

        persistence.save(users, finances);

        LoadedState loaded = persistence.load();

        UserFinance restoredAlex = loaded.finances()
            .getOrCreate(alex.id());

        UserFinance restoredBob = loaded.finances()
            .getOrCreate(bob.id());

        Transaction restoredExpense = restoredAlex.wallet()
            .getTransactions()
            .stream()
            .filter(transaction -> transaction.transferId() != null)
            .findFirst()
            .orElseThrow();

        Transaction restoredIncome = restoredBob.wallet()
            .getTransactions()
            .getFirst();

        assertEquals(expenseId, restoredExpense.id());
        assertEquals(incomeId, restoredIncome.id());

        assertEquals(transferId, restoredExpense.transferId());
        assertEquals(transferId, restoredIncome.transferId());

        assertEquals(
            restoredExpense.transferId(),
            restoredIncome.transferId()
        );

        assertEquals(createdAt, restoredExpense.createdAt());
        assertEquals(createdAt, restoredIncome.createdAt());

        assertEquals(
            0,
            new BigDecimal("3500").compareTo(
                restoredAlex.wallet().getBalance()
            )
        );

        assertEquals(
            0,
            new BigDecimal("1500").compareTo(
                restoredBob.wallet().getBalance()
            )
        );
    }
}
