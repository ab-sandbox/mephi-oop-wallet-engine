package ru.mephi.promoit.wallet.domain.model;

import java.math.BigDecimal;

public record FinanceSummary(
    BigDecimal income,
    BigDecimal expenses,
    BigDecimal balance
) {
}
