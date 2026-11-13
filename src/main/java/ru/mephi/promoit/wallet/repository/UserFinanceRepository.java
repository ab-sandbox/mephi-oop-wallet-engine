package ru.mephi.promoit.wallet.repository;

import ru.mephi.promoit.wallet.domain.model.UserFinance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserFinanceRepository {

    private final Map<UUID, UserFinance> finances = new HashMap<>();

    public UserFinance getOrCreate(UUID userId) {
        return finances.computeIfAbsent(
            userId,
            ignored -> new UserFinance()
        );
    }

    public Map<UUID, UserFinance> findAll() {
        return Map.copyOf(finances);
    }

    public void restore(UUID userId, UserFinance finance) {
        if (userId == null || finance == null) {
            throw new IllegalArgumentException(
                "User ID and finance must not be null"
            );
        }

        if (finances.putIfAbsent(userId, finance) != null) {
            throw new IllegalArgumentException(
                "User finances already exist: " + userId
            );
        }
    }
}
