package ru.mephi.promoit.wallet.cli.command;

public record RegisterCommand(
    String login,
    String password
) implements FinanceCommand {
}
