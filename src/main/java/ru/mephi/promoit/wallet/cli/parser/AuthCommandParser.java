package ru.mephi.promoit.wallet.cli.parser;

import ru.mephi.promoit.wallet.cli.command.*;

public class AuthCommandParser implements CommandGroupParser {

    @Override
    public FinanceCommand parse(String[] parts) {
        return switch (parts[0]) {
            case "register" -> parseRegister(parts);
            case "login" -> parseLogin(parts);
            case "logout" -> parseLogout(parts);
            default -> throw new IllegalArgumentException(
                "Unknown authentication command: " + parts[0]
            );
        };
    }

    private FinanceCommand parseRegister(String[] parts) {
        CommandArguments.requireLength(parts, 3);

        return new RegisterCommand(parts[1], parts[2]);
    }

    private FinanceCommand parseLogin(String[] parts) {
        CommandArguments.requireLength(parts, 3);

        return new LoginCommand(parts[1], parts[2]);
    }

    private FinanceCommand parseLogout(String[] parts) {
        CommandArguments.requireLength(parts, 1);

        return new LogoutCommand();
    }
}
