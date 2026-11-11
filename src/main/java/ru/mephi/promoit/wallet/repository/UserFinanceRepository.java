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
}
