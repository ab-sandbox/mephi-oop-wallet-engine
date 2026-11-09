package ru.mephi.promoit.wallet.cli;

import ru.mephi.promoit.wallet.cli.command.*;
import ru.mephi.promoit.wallet.model.CategoryType;

import java.math.BigDecimal;
import java.util.Locale;

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

        if (parts.length == 2) {
            if (parts[0].equals("category")
                && parts[1].equals("list")) {
                return new ListCategoriesCommand();
            }

            if (parts[0].equals("budget")
                && parts[1].equals("list")) {
                return new ListBudgetsCommand();
            }
        }

        if (parts.length == 4) {
            if (parts[0].equals("category")
                && parts[1].equals("add")) {
                return new AddCategoryCommand(
                    parts[3],
                    parseCategoryType(parts[2])
                );
            }

            if (parts[0].equals("budget")
                && parts[1].equals("set")) {
                return new SetBudgetCommand(
                    parts[2],
                    parseAmount(parts[3])
                );
            }

            if (parts[1].equals("add")) {
                BigDecimal amount = parseAmount(parts[3]);

                return switch (parts[0]) {
                    case "income" -> new AddIncomeCommand(parts[2], amount);
                    case "expense" -> new AddExpenseCommand(parts[2], amount);
                    default -> throw new IllegalArgumentException("Unknown command");
                };
            }
        }

        throw new IllegalArgumentException("Invalid command syntax");
    }

    private CategoryType parseCategoryType(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "income" -> CategoryType.INCOME;
            case "expense" -> CategoryType.EXPENSE;
            default -> throw new IllegalArgumentException(
                "Unknown category type: " + value
            );
        };
    }

    private BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: " + value, e);
        }
    }
}
