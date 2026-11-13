package ru.mephi.promoit.wallet.cli.parser;

import ru.mephi.promoit.wallet.cli.command.AddCategoryCommand;
import ru.mephi.promoit.wallet.cli.command.FinanceCommand;
import ru.mephi.promoit.wallet.cli.command.ListCategoriesCommand;
import ru.mephi.promoit.wallet.cli.command.RenameCategoryCommand;
import ru.mephi.promoit.wallet.domain.model.CategoryType;

import java.util.Locale;

public class CategoryCommandParser implements CommandGroupParser {

    @Override
    public FinanceCommand parse(String[] parts) {
        CommandArguments.requireMinLength(parts, 2);

        return switch (parts[1]) {
            case "add" -> parseAdd(parts);
            case "list" -> parseList(parts);
            case "rename" -> parseRename(parts);
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

    private FinanceCommand parseRename(String[] parts) {
        CommandArguments.requireLength(parts, 5);

        return new RenameCategoryCommand(
            parseCategoryType(parts[2]),
            parts[3],
            parts[4]
        );
    }
}
