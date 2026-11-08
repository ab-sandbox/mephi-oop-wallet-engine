package ru.mephi.promoit.wallet.cli.command;

public sealed interface FinanceCommand
    permits AddIncomeCommand,
    AddExpenseCommand,
    AddCategoryCommand,
    ListCategoriesCommand,
    ShowBalanceCommand,
    ExitCommand,
    HelpCommand {
}
