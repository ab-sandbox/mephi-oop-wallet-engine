package ru.mephi.promoit.wallet.domain.model;

import java.math.BigDecimal;

public record CategoryReport(
    String category,
    BigDecimal amount
) {
}
