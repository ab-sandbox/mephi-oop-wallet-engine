package ru.mephi.promoit.wallet.cli.parser;

import ru.mephi.promoit.wallet.cli.command.*;
import ru.mephi.promoit.wallet.model.CategoryType;

import java.util.Locale;

public class CategoryCommandParser implements CommandGroupParser {

    @Override
    public FinanceCommand parse(String[] parts) {
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                "Category action is required"
            );
        }

        return switch (parts[1]) {
            case "add" -> parseAdd(parts);
            case "list" -> parseList(parts);
            default -> throw new IllegalArgumentException(
                "Unknown category action: " + parts[1]
            );
        };
    }

    private FinanceCommand parseAdd(String[] parts) {
        CommandArguments.requireLength(parts, 4);

        return new AddCategoryCommand(
            parts[3],
            parseCategoryType(parts[2])
        );
    }

    private FinanceCommand parseList(String[] parts) {
        CommandArguments.requireLength(parts, 2);
        return new ListCategoriesCommand();
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
}
