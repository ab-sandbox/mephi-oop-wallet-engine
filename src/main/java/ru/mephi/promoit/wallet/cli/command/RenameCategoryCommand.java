package ru.mephi.promoit.wallet.cli.command;

import ru.mephi.promoit.wallet.domain.model.CategoryType;

public record RenameCategoryCommand(
    CategoryType type,
    String currentName,
    String newName
) implements FinanceCommand {
}
