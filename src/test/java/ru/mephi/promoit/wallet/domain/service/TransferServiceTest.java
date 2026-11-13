package ru.mephi.promoit.wallet.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.domain.model.CategoryType;
import ru.mephi.promoit.wallet.domain.model.SystemCategory;
import ru.mephi.promoit.wallet.domain.model.Transaction;
import ru.mephi.promoit.wallet.domain.model.TransactionType;
import ru.mephi.promoit.wallet.domain.model.User;
import ru.mephi.promoit.wallet.domain.model.UserFinance;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferServiceTest {

    private AuthService authService;
    private UserFinanceRepository finances;
    private TransferService transferService;

    private User sender;
    private User recipient;

    @BeforeEach
    void setUp() {
        UserRepository users = new UserRepository();
        finances = new UserFinanceRepository();

        authService = new AuthService(users, new PasswordHasher());
        transferService = new TransferService(authService, users, finances);

        sender = authService.register("alex", "password123");
        recipient = authService.register("bob", "password123");

        authService.login("alex", "password123");

        UserFinance senderFinance = finances.getOrCreate(sender.id());

        FinanceService financeService = new FinanceService(
            senderFinance.wallet(),
            senderFinance.categories()
        );

        financeService.addCategory("Salary", CategoryType.INCOME);
        financeService.addIncome("Salary", new BigDecimal("5000"));
    }

    @Test
    void shouldTransferMoneyBetweenUsers() {
        transferService.transfer("bob", new BigDecimal("1500"));

        UserFinance senderFinance = finances.getOrCreate(sender.id());
        UserFinance recipientFinance = finances.getOrCreate(recipient.id());

        assertEquals(
            new BigDecimal("3500"),
            senderFinance.wallet().getBalance()
        );

        assertEquals(
            new BigDecimal("1500"),
            recipientFinance.wallet().getBalance()
        );

        Transaction expense = senderFinance.wallet().getTransactions()
            .stream()
            .filter(t -> t.type() == TransactionType.EXPENSE)
            .findFirst()
            .orElseThrow();

        Transaction income = recipientFinance.wallet().getTransactions()
            .getFirst();

        assertNotNull(expense.transferId());
        assertEquals(expense.transferId(), income.transferId());
        assertNotEquals(expense.id(), income.id());

        assertEquals(TransactionType.EXPENSE, expense.type());
        assertEquals(TransactionType.INCOME, income.type());

        assertEquals(expense.amount(), income.amount());
        assertEquals(expense.createdAt(), income.createdAt());

        assertEquals(
            SystemCategory.TRANSFER_OUT.categoryName(),
            senderFinance.categories()
                .findByName("TransferOut", CategoryType.EXPENSE)
                .name()
        );

        assertEquals(
            SystemCategory.TRANSFER_IN.categoryName(),
            recipientFinance.categories()
                .findByName("TransferIn", CategoryType.INCOME)
                .name()
        );
    }

    @Test
    void shouldRejectInsufficientBalance() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer("bob", new BigDecimal("6000"))
        );

        assertEquals("Insufficient balance", exception.getMessage());

        assertEquals(
            new BigDecimal("5000"),
            finances.getOrCreate(sender.id()).wallet().getBalance()
        );

        assertTrue(
            finances.getOrCreate(recipient.id())
                .wallet().getTransactions().isEmpty()
        );
    }

    @Test
    void shouldRejectUnknownRecipient() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer("unknown", new BigDecimal("1000"))
        );

        assertEquals("User not found: unknown", exception.getMessage());

        assertEquals(
            new BigDecimal("5000"),
            finances.getOrCreate(sender.id()).wallet().getBalance()
        );
    }

    @Test
    void shouldRejectSelfTransfer() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer("alex", new BigDecimal("1000"))
        );

        assertEquals("Cannot transfer to yourself", exception.getMessage());

        assertEquals(
            new BigDecimal("5000"),
            finances.getOrCreate(sender.id()).wallet().getBalance()
        );
    }

    @Test
    void shouldRejectInvalidAmount() {
        List<BigDecimal> invalidAmounts = List.of(
            BigDecimal.ZERO,
            new BigDecimal("-100")
        );

        for (BigDecimal amount : invalidAmounts) {
            assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer("bob", amount)
            );
        }

        assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer("bob", null)
        );

        assertEquals(
            new BigDecimal("5000"),
            finances.getOrCreate(sender.id()).wallet().getBalance()
        );
    }

    @Test
    void shouldReuseSystemCategories() {
        transferService.transfer("bob", new BigDecimal("1000"));
        transferService.transfer("bob", new BigDecimal("500"));

        UserFinance senderFinance = finances.getOrCreate(sender.id());
        UserFinance recipientFinance = finances.getOrCreate(recipient.id());

        long outCategories = senderFinance.categories().findAll()
            .stream()
            .filter(c -> c.type() == CategoryType.EXPENSE)
            .filter(c -> c.name().equals("TransferOut"))
            .count();

        long inCategories = recipientFinance.categories().findAll()
            .stream()
            .filter(c -> c.type() == CategoryType.INCOME)
            .filter(c -> c.name().equals("TransferIn"))
            .count();

        assertEquals(1, outCategories);
        assertEquals(1, inCategories);

        assertEquals(
            new BigDecimal("3500"),
            senderFinance.wallet().getBalance()
        );

        assertEquals(
            new BigDecimal("1500"),
            recipientFinance.wallet().getBalance()
        );

        List<Transaction> transfers = senderFinance.wallet()
            .getTransactions().stream()
            .filter(t -> t.transferId() != null)
            .toList();

        assertEquals(2, transfers.size());
        assertNotEquals(
            transfers.get(0).transferId(),
            transfers.get(1).transferId()
        );
    }
}
