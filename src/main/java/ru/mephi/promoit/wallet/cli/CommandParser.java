package ru.mephi.promoit.wallet.cli;

import ru.mephi.promoit.wallet.cli.command.*;

import java.math.BigDecimal;

public class CommandParser {

    public FinanceCommand parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Command must not be blank");
        }

        String[] parts = input.trim().split("\\s+");

        if (parts.length == 1) {
            return switch (parts[0]) {
                case "balance" -> new ShowBalanceCommand();
                case "help" -> new HelpCommand();
                case "exit" -> new ExitCommand();
                default -> throw new IllegalArgumentException("Unknown command");
            };
        }

        if (parts.length == 4
            && parts[0].equals("income")
            && parts[1].equals("add")) {

            try {
                return new AddIncomeCommand(
                    parts[2],
                    new BigDecimal(parts[3])
                );
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid amount", e);
            }
        }

        throw new IllegalArgumentException("Invalid command syntax");
    }
}
