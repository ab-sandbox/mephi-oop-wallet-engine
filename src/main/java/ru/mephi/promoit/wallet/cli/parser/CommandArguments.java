package ru.mephi.promoit.wallet.cli.parser;

import java.math.BigDecimal;

public final class CommandArguments {

    private CommandArguments() {
    }

    public static void requireLength(String[] parts, int expected) {
        if (parts.length != expected) {
            throw new IllegalArgumentException(
                "Expected " + expected
                + " command arguments, got " + parts.length
            );
        }
    }

    public static void requireMinLength(String[] parts, int minimum) {
        if (parts.length < minimum) {
            throw new IllegalArgumentException(
                "Expected at least " + minimum
                + " command arguments, got " + parts.length
            );
        }
    }

    public static BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Invalid amount: " + value, e
            );
        }
    }
}
