package ru.mephi.promoit.wallet.repository;

import ru.mephi.promoit.wallet.domain.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserRepository {

    private final Map<String, User> users = new HashMap<>();

    public void save(User user) {
        if (users.containsKey(user.login())) {
            throw new IllegalArgumentException(
                "User already exists: " + user.login()
            );
        }

        users.put(user.login(), user);
    }

    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(users.get(login));
    }
}
