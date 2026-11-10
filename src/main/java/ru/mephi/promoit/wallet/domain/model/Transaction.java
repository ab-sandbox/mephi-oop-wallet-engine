package ru.mephi.promoit.wallet.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(
    UUID id,
    TransactionType type,
    UUID categoryId,
    BigDecimal amount,
    Instant createdAt,
    UUID transferId
) {
    public Transaction {
        if (id == null) {
            throw new IllegalArgumentException("Transaction ID must not be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("Transaction type must not be null");
        }

        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID must not be null");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Creation time must not be null");
        }
    }
}
