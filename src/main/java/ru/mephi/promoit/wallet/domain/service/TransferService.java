package ru.mephi.promoit.wallet.domain.service;

import ru.mephi.promoit.wallet.domain.model.Category;
import ru.mephi.promoit.wallet.domain.model.SystemCategory;
import ru.mephi.promoit.wallet.domain.model.Transaction;
import ru.mephi.promoit.wallet.domain.model.TransactionType;
import ru.mephi.promoit.wallet.domain.model.User;
import ru.mephi.promoit.wallet.domain.model.UserFinance;
import ru.mephi.promoit.wallet.repository.CategoryCatalog;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TransferService {

    private final AuthService authService;
    private final UserRepository users;
    private final UserFinanceRepository finances;

    public TransferService(
        AuthService authService,
        UserRepository users,
        UserFinanceRepository finances
    ) {
        this.authService = authService;
        this.users = users;
        this.finances = finances;
    }

    public synchronized void transfer(String recipientLogin, BigDecimal amount) {
        User sender = authService.requireCurrentUser();

        if (recipientLogin == null || recipientLogin.isBlank()) {
            throw new IllegalArgumentException("Recipient login is required");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        User recipient = users.findByLogin(recipientLogin.trim())
            .orElseThrow(() -> new IllegalArgumentException(
                "User not found: " + recipientLogin
            ));

        if (sender.id().equals(recipient.id())) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        UserFinance senderFinance = finances.getOrCreate(sender.id());
        UserFinance recipientFinance = finances.getOrCreate(recipient.id());

        if (senderFinance.wallet().getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        Category outCategory = findOrCreateCategory(
            senderFinance.categories(),
            SystemCategory.TRANSFER_OUT
        );

        Category inCategory = findOrCreateCategory(
            recipientFinance.categories(),
            SystemCategory.TRANSFER_IN
        );

        UUID transferId = UUID.randomUUID();
        Instant now = Instant.now();

        Transaction expense = new Transaction(
            UUID.randomUUID(),
            TransactionType.EXPENSE,
            outCategory.id(),
            amount,
            now,
            transferId
        );

        Transaction income = new Transaction(
            UUID.randomUUID(),
            TransactionType.INCOME,
            inCategory.id(),
            amount,
            now,
            transferId
        );

        senderFinance.wallet().addTransaction(expense);
        recipientFinance.wallet().addTransaction(income);
    }

    private Category findOrCreateCategory(
        CategoryCatalog catalog,
        SystemCategory systemCategory
    ) {
        return catalog.findAll().stream()
            .filter(category ->
                category.type() == systemCategory.categoryType()
            )
            .filter(category ->
                category.name().equalsIgnoreCase(systemCategory.categoryName())
            )
            .findFirst()
            .orElseGet(() -> catalog.add(
                systemCategory.categoryName(),
                systemCategory.categoryType()
            ));
    }
}
