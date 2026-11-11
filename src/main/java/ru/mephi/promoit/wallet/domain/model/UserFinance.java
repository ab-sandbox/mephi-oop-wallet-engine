package ru.mephi.promoit.wallet.domain.model;

import ru.mephi.promoit.wallet.repository.BudgetCatalog;
import ru.mephi.promoit.wallet.repository.CategoryCatalog;

public record UserFinance(
    Wallet wallet,
    CategoryCatalog categories,
    BudgetCatalog budgets
) {
    public UserFinance() {
        this(
            new Wallet(),
            new CategoryCatalog(),
            new BudgetCatalog()
        );
    }
}
