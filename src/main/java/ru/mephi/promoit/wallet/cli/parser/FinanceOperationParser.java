package ru.mephi.promoit.wallet.cli.parser;

import ru.mephi.promoit.wallet.cli.command.AddExpenseCommand;
import ru.mephi.promoit.wallet.cli.command.AddIncomeCommand;
import ru.mephi.promoit.wallet.cli.command.FinanceCommand;

import java.math.BigDecimal;

public class FinanceOperationParser implements CommandGroupParser {

    @Override
    public FinanceCommand parse(String[] parts) {
        CommandArguments.requireLength(parts, 4);

        if (!parts[1].equals("add")) {
            throw new IllegalArgumentException(
                "Unknown operation: " + parts[1]
            );
        }

        String category = parts[2];
        BigDecimal amount = CommandArguments.parseAmount(parts[3]);

        return switch (parts[0]) {
            case "income" -> new AddIncomeCommand(category, amount);
            case "expense" -> new AddExpenseCommand(category, amount);
            default -> throw new IllegalArgumentException(
                "Unknown financial operation: " + parts[0]
            );
        };
    }
}
