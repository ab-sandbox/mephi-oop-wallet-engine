package ru.mephi.promoit.wallet.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.domain.model.User;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private AuthService authService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        authService = new AuthService(
            userRepository,
            new PasswordHasher()
        );
    }

    @Test
    void shouldRegisterUser() {
        User user = authService.register("alex", "password123");

        assertNotNull(user.id());
        assertEquals("alex", user.login());
        assertNotEquals("password123", user.passwordHash());
        assertTrue(userRepository.findByLogin("alex").isPresent());
    }

    @Test
    void shouldRejectDuplicateLogin() {
        authService.register("alex", "password123");

        assertThrows(
            IllegalArgumentException.class,
            () -> authService.register("alex", "anotherPassword")
        );
    }

    @Test
    void shouldLoginWithValidCredentials() {
        User registered = authService.register("alex", "password123");

        User loggedIn = authService.login("alex", "password123");

        assertEquals(registered.id(), loggedIn.id());
        assertEquals(loggedIn, authService.requireCurrentUser());
    }

    @Test
    void shouldRejectInvalidPassword() {
        authService.register("alex", "password123");

        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login("alex", "wrongPassword")
        );
    }

    @Test
    void shouldRejectUnknownUser() {
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login("unknown", "password123")
        );
    }

    @Test
    void shouldRequireAuthentication() {
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.requireCurrentUser()
        );
    }

    @Test
    void shouldLogout() {
        authService.register("alex", "password123");
        authService.login("alex", "password123");

        authService.logout();

        assertThrows(
            IllegalArgumentException.class,
            () -> authService.requireCurrentUser()
        );
    }

    @Test
    void shouldNotStorePlainTextPassword() {
        User user = authService.register("alex", "password123");

        assertFalse(user.passwordHash().contains("password123"));
    }
}
