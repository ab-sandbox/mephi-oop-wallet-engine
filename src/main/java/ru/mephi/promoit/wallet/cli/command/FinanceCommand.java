package ru.mephi.promoit.wallet.cli.command;

public sealed interface FinanceCommand
    permits AddCategoryCommand, AddExpenseCommand, AddIncomeCommand,
    ExitCommand, HelpCommand, ListBudgetsCommand, ListCategoriesCommand,
    SetBudgetCommand, ShowBalanceCommand {
}
