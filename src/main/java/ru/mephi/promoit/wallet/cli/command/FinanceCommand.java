package ru.mephi.promoit.wallet.cli.command;

public sealed interface FinanceCommand
    permits AddIncomeCommand, ShowBalanceCommand, ExitCommand, HelpCommand {
}
