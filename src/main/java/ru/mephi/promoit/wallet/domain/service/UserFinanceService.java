package ru.mephi.promoit.wallet.domain.service;

import ru.mephi.promoit.wallet.domain.model.User;
import ru.mephi.promoit.wallet.domain.model.UserFinance;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;

public class UserFinanceService {

    private final AuthService authService;
    private final UserFinanceRepository repository;

    public UserFinanceService(
        AuthService authService,
        UserFinanceRepository repository
    ) {
        this.authService = authService;
        this.repository = repository;
    }

    public UserFinance current() {
        User user = authService.requireCurrentUser();
        return repository.getOrCreate(user.id());
    }
}
