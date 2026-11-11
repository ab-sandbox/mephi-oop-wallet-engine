package ru.mephi.promoit.wallet.domain.service;

import ru.mephi.promoit.wallet.domain.model.User;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

import java.util.UUID;

public class AuthService {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    private User currentUser;

    public AuthService(
        UserRepository users,
        PasswordHasher passwordHasher
    ) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    public User register(String login, String password) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login is required");
        }

        login = login.trim();

        if (users.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException(
                "User already exists: " + login
            );
        }

        User user = new User(
            UUID.randomUUID(),
            login,
            passwordHasher.hash(password)
        );

        users.save(user);
        return user;
    }

    public User login(String login, String password) {
        if (currentUser != null) {
            throw new IllegalStateException("Already logged in");
        }

        User user = users.findByLogin(login)
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid credentials")
            );

        if (!passwordHasher.matches(password, user.passwordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        currentUser = user;
        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public User requireCurrentUser() {
        if (currentUser == null) {
            throw new IllegalArgumentException(
                "Authentication required"
            );
        }

        return currentUser;
    }
}
