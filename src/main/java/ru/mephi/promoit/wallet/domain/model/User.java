package ru.mephi.promoit.wallet.domain.model;

import java.util.UUID;

public record User(
    UUID id,
    String login,
    String passwordHash
) {
    public User {
        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login is required");
        }

        if (login.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Login must not contain whitespace");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }

        login = login.trim();
    }
}
