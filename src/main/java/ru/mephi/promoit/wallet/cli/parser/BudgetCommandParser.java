package ru.mephi.promoit.wallet.cli.parser;

import ru.mephi.promoit.wallet.cli.command.FinanceCommand;
import ru.mephi.promoit.wallet.cli.command.ListBudgetsCommand;
import ru.mephi.promoit.wallet.cli.command.SetBudgetCommand;

public class BudgetCommandParser implements CommandGroupParser {

    @Override
    public FinanceCommand parse(String[] parts) {
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                "Budget action is required"
            );
        }

        return switch (parts[1]) {
            case "set" -> parseSet(parts);
            case "list" -> parseList(parts);
            default -> throw new IllegalArgumentException(
                "Unknown budget action: " + parts[1]
            );
        };
    }

    private FinanceCommand parseSet(String[] parts) {
        CommandArguments.requireLength(parts, 4);

        return new SetBudgetCommand(
            parts[2],
            CommandArguments.parseAmount(parts[3])
        );
    }

    private FinanceCommand parseList(String[] parts) {
        CommandArguments.requireLength(parts, 2);
        return new ListBudgetsCommand();
    }
}
