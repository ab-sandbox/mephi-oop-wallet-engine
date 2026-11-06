package ru.mephi.promoit.wallet.model;

import java.util.UUID;

public record Category(
    UUID id,
    String name,
    CategoryType type
) {
    public Category {
        if (id == null) {
            throw new IllegalArgumentException("Category ID must not be null");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank");
        }

        if (type == null) {
            throw new IllegalArgumentException("Category type must not be null");
        }

        name = name.trim();
    }
}
