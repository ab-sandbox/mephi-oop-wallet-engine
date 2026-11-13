package ru.mephi.promoit.wallet.persistence;

import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;

public record LoadedState(
    UserRepository users,
    UserFinanceRepository finances
) {
}
