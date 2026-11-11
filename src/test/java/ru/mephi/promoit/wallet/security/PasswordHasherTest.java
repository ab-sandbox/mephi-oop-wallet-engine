package ru.mephi.promoit.wallet.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void shouldVerifyCorrectPassword() {
        String hash = hasher.hash("password123");

        assertTrue(hasher.matches("password123", hash));
    }

    @Test
    void shouldRejectIncorrectPassword() {
        String hash = hasher.hash("password123");

        assertFalse(hasher.matches("wrongPassword", hash));
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String first = hasher.hash("password123");
        String second = hasher.hash("password123");

        assertNotEquals(first, second);
        assertTrue(hasher.matches("password123", first));
        assertTrue(hasher.matches("password123", second));
    }

    @Test
    void shouldRejectMalformedHash() {
        assertFalse(hasher.matches("password123", "invalid-hash"));
    }

    @Test
    void shouldRejectBlankPassword() {
        assertThrows(
            IllegalArgumentException.class,
            () -> hasher.hash(" ")
        );
    }
}
