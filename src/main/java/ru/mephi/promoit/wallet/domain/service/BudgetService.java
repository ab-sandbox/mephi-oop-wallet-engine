package ru.mephi.promoit.wallet.domain.service;

import ru.mephi.promoit.wallet.domain.model.*;
import ru.mephi.promoit.wallet.repository.BudgetCatalog;
import ru.mephi.promoit.wallet.repository.CategoryCatalog;

import java.math.BigDecimal;
import java.util.List;

public class BudgetService {

    private final BudgetCatalog budgets;
    private final CategoryCatalog categories;
    private final Wallet wallet;

    public BudgetService(
        BudgetCatalog budgets,
        CategoryCatalog categories,
        Wallet wallet
    ) {
        this.budgets = budgets;
        this.categories = categories;
        this.wallet = wallet;
    }

    public void setBudget(String categoryName, BigDecimal limit) {
        Category category = categories.findByName(
            categoryName,
            CategoryType.EXPENSE
        );

        budgets.set(new Budget(category.id(), limit));
    }

    public BigDecimal getSpent(Budget budget) {
        return wallet.getTransactions().stream()
            .filter(t -> t.type() == TransactionType.EXPENSE)
            .filter(t -> t.categoryId().equals(budget.categoryId()))
            .map(Transaction::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getRemaining(Budget budget) {
        return budget.remaining(getSpent(budget));
    }

    public List<Budget> getBudgets() {
        return budgets.findAll();
    }

    public Category getCategory(Budget budget) {
        return categories.findAll().stream()
            .filter(c -> c.id().equals(budget.categoryId()))
            .findFirst()
            .orElseThrow(() ->
                new IllegalArgumentException("Category not found")
            );
    }

    public String getWarning(String categoryName) {
        Category category = categories.findByName(
            categoryName,
            CategoryType.EXPENSE
        );

        Budget budget = budgets.findByCategory(category.id());

        if (budget == null) {
            return null;
        }

        BigDecimal spent = getSpent(budget);
        BigDecimal remaining = budget.remaining(spent);

        if (remaining.signum() < 0) {
            return "Warning: " + category.name()
                   + " budget exceeded by "
                   + remaining.abs();
        }

        BigDecimal threshold = budget.limit()
            .multiply(new BigDecimal("0.80"));

        if (spent.compareTo(threshold) >= 0) {
            return "Warning: 80% or more of "
                   + category.name() + " budget used";
        }

        return null;
    }
}
