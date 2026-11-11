package ru.mephi.promoit.wallet.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.domain.model.CategoryType;
import ru.mephi.promoit.wallet.domain.model.UserFinance;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserFinanceServiceTest {

    private AuthService authService;
    private UserFinanceService userFinanceService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            new UserRepository(),
            new PasswordHasher()
        );

        userFinanceService = new UserFinanceService(
            authService,
            new UserFinanceRepository()
        );
    }

    @Test
    void shouldIsolateUserFinances() {
        authService.register("alex", "password123");
        authService.register("bob", "password456");

        authService.login("alex", "password123");

        UserFinance alexFinance = userFinanceService.current();

        FinanceService alexService = new FinanceService(
            alexFinance.wallet(),
            alexFinance.categories()
        );

        alexService.addCategory("Salary", CategoryType.INCOME);
        alexService.addIncome("Salary", new BigDecimal("50000"));

        authService.logout();

        authService.login("bob", "password456");

        UserFinance bobFinance = userFinanceService.current();

        assertNotSame(alexFinance.wallet(), bobFinance.wallet());
        assertEquals(
            0,
            BigDecimal.ZERO.compareTo(bobFinance.wallet().getBalance())
        );

        authService.logout();
        authService.login("alex", "password123");

        assertSame(alexFinance, userFinanceService.current());

        assertEquals(
            0,
            new BigDecimal("50000")
                .compareTo(userFinanceService.current().wallet().getBalance())
        );
    }

    @Test
    void shouldRejectAccessWithoutAuthentication() {
        assertThrows(
            IllegalArgumentException.class,
            () -> userFinanceService.current()
        );
    }
}
