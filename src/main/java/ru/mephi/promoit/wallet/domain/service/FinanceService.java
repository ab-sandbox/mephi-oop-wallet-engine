package ru.mephi.promoit.wallet.domain.service;

import ru.mephi.promoit.wallet.domain.model.*;
import ru.mephi.promoit.wallet.repository.CategoryCatalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FinanceService {

    private final Wallet wallet;
    private final CategoryCatalog categoryCatalog;

    public FinanceService(Wallet wallet, CategoryCatalog categoryCatalog) {
        this.wallet = wallet;
        this.categoryCatalog = categoryCatalog;
    }

    public Category addCategory(String name, CategoryType type) {
        return categoryCatalog.add(name, type);
    }

    public List<Category> getCategories() {
        return categoryCatalog.findAll();
    }

    public void addIncome(String category, BigDecimal amount) {
        addTransaction(category, amount, TransactionType.INCOME);
    }

    public void addExpense(String category, BigDecimal amount) {
        addTransaction(category, amount, TransactionType.EXPENSE);
    }

    public BigDecimal getBalance() {
        return wallet.getBalance();
    }

    private void addTransaction(
        String categoryName,
        BigDecimal amount,
        TransactionType type
    ) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        CategoryType categoryType = switch (type) {
            case INCOME -> CategoryType.INCOME;
            case EXPENSE -> CategoryType.EXPENSE;
        };

        Category category = categoryCatalog.findByName(
            categoryName,
            categoryType
        );

        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            type,
            category.id(),
            amount,
            Instant.now(),
            null
        );

        wallet.addTransaction(transaction);
    }
}
