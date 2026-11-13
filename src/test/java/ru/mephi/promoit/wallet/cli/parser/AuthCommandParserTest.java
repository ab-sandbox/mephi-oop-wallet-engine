package ru.mephi.promoit.wallet.cli.parser;

import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.cli.command.LoginCommand;
import ru.mephi.promoit.wallet.cli.command.LogoutCommand;
import ru.mephi.promoit.wallet.cli.command.RegisterCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthCommandParserTest {

    private final CommandParser parser = new CommandParser();

    @Test
    void shouldParseRegister() {
        assertEquals(
            new RegisterCommand("alex", "password123"),
            parser.parse("register alex password123")
        );
    }

    @Test
    void shouldParseLogin() {
        assertEquals(
            new LoginCommand("alex", "password123"),
            parser.parse("login alex password123")
        );
    }

    @Test
    void shouldParseLogout() {
        assertEquals(
            new LogoutCommand(),
            parser.parse("logout")
        );
    }

    @Test
    void shouldRejectRegisterWithoutPassword() {
        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse("register alex")
        );
    }

    @Test
    void shouldRejectLoginWithoutPassword() {
        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse("login alex")
        );
    }

    @Test
    void shouldRejectLogoutWithArguments() {
        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse("logout alex")
        );
    }
}
