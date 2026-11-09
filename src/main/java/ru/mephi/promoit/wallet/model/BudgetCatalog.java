package ru.mephi.promoit.wallet.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class BudgetCatalog {

    private final Map<UUID, Budget> budgets = new HashMap<>();

    public void set(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Budget must not be null");
        }

        budgets.put(budget.categoryId(), budget);
    }

    public List<Budget> findAll() {
        return List.copyOf(budgets.values());
    }

    public Budget findByCategory(UUID categoryId) {
        return budgets.get(categoryId);
    }
}
