package ru.mephi.promoit.wallet.cli.command;

public sealed interface FinanceCommand
    permits AddCategoryCommand,
    AddExpenseCommand,
    AddIncomeCommand,
    CategoryReportCommand,
    ExitCommand,
    HelpCommand,
    ListBudgetsCommand,
    ListCategoriesCommand,
    LoginCommand,
    LogoutCommand,
    RegisterCommand,
    SetBudgetCommand,
    ShowBalanceCommand,
    SummaryCommand {
}
