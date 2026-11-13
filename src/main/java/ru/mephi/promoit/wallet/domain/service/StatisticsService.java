package ru.mephi.promoit.wallet.domain.service;

import ru.mephi.promoit.wallet.domain.model.*;
import ru.mephi.promoit.wallet.repository.CategoryCatalog;

import java.math.BigDecimal;
import java.util.*;

public class StatisticsService {

    private final Wallet wallet;
    private final CategoryCatalog categories;

    public StatisticsService(Wallet wallet, CategoryCatalog categories) {
        this.wallet = wallet;
        this.categories = categories;
    }

    public FinanceSummary getSummary() {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;

        for (Transaction transaction : wallet.getTransactions()) {
            if (transaction.type() == TransactionType.INCOME) {
                income = income.add(transaction.amount());
            } else if (transaction.type() == TransactionType.EXPENSE) {
                expenses = expenses.add(transaction.amount());
            }
        }

        return new FinanceSummary(
            income,
            expenses,
            income.subtract(expenses)
        );
    }

    public List<CategoryReport> getCategoryReport(TransactionType type) {
        return getCategoryReport(type, List.of());
    }

    public List<CategoryReport> getCategoryReport(
        TransactionType type,
        List<String> selectedCategories
    ) {
        Map<UUID, BigDecimal> totals = new HashMap<>();

        for (Transaction transaction : wallet.getTransactions()) {
            if (transaction.type() != type) {
                continue;
            }

            UUID categoryId = transaction.categoryId();

            BigDecimal current = totals.getOrDefault(
                categoryId,
                BigDecimal.ZERO
            );

            totals.put(categoryId, current.add(transaction.amount()));
        }

        List<CategoryReport> report = new ArrayList<>();

        CategoryType categoryType = switch (type) {
            case INCOME -> CategoryType.INCOME;
            case EXPENSE -> CategoryType.EXPENSE;
        };

        if (selectedCategories.isEmpty()) {
            for (Category category : categories.findAll()) {
                BigDecimal amount = totals.get(category.id());

                if (category.type() == categoryType && amount != null) {
                    report.add(new CategoryReport(category.name(), amount));
                }
            }
        } else {
            for (String name : new LinkedHashSet<>(selectedCategories)) {
                Category category = categories.findByName(name, categoryType);

                BigDecimal amount = totals.getOrDefault(
                    category.id(),
                    BigDecimal.ZERO
                );

                report.add(new CategoryReport(category.name(), amount));
            }
        }

        report.sort(
            Comparator.comparing(CategoryReport::category)
        );

        return report;
    }
}
