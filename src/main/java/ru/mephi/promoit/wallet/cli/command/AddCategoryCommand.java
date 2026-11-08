package ru.mephi.promoit.wallet.cli.command;

import ru.mephi.promoit.wallet.model.CategoryType;

public record AddCategoryCommand(String name, CategoryType type) implements FinanceCommand {
}
