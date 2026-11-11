package ru.mephi.promoit.wallet.cli.command;

public record LoginCommand(
    String login,
    String password
) implements FinanceCommand {
}
