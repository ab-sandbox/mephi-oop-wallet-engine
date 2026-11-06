package ru.mephi.promoit.wallet.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Budget(
    UUID categoryId,
    BigDecimal limit
) {
    public Budget {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID must not be null");
        }

        if (limit == null || limit.signum() <= 0) {
            throw new IllegalArgumentException("Budget limit must be positive");
        }
    }

    public BigDecimal remaining(BigDecimal spent) {
        if (spent == null || spent.signum() < 0) {
            throw new IllegalArgumentException("Spent amount must not be negative");
        }

        return limit.subtract(spent);
    }

    public boolean isExceeded(BigDecimal spent) {
        return remaining(spent).signum() < 0;
    }
}
