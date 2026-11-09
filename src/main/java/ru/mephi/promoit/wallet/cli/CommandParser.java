package ru.mephi.promoit.wallet.cli;

import ru.mephi.promoit.wallet.cli.command.*;
import ru.mephi.promoit.wallet.cli.parser.*;

import java.util.Map;

public class CommandParser {

    private final Map<String, CommandGroupParser> parsers;

    public CommandParser() {
        CommandGroupParser financeParser = new FinanceOperationParser();

        parsers = Map.of(
            "income", financeParser,
            "expense", financeParser,
            "category", new CategoryCommandParser(),
            "budget", new BudgetCommandParser()
        );
    }

    public FinanceCommand parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                "Command must not be blank"
            );
        }

        String[] parts = input.trim().split("\\s+");

        if (parts.length == 1) {
            return switch (parts[0]) {
                case "balance" -> new ShowBalanceCommand();
                case "help" -> new HelpCommand();
                case "exit" -> new ExitCommand();
                default -> parseGroup(parts);
            };
        }

        return parseGroup(parts);
    }

    private FinanceCommand parseGroup(String[] parts) {
        CommandGroupParser parser = parsers.get(parts[0]);

        if (parser == null) {
            throw new IllegalArgumentException(
                "Unknown command: " + parts[0]
            );
        }

        return parser.parse(parts);
    }
}
