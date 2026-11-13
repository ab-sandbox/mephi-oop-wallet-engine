package ru.mephi.promoit.wallet.persistence.dto;

import ru.mephi.promoit.wallet.domain.model.Budget;
import ru.mephi.promoit.wallet.domain.model.Category;
import ru.mephi.promoit.wallet.domain.model.Transaction;

import java.util.List;
import java.util.UUID;

public record UserFinanceData(
    UUID userId,
    List<Transaction> transactions,
    List<Category> categories,
    List<Budget> budgets
) {
}
