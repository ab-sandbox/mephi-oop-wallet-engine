package ru.mephi.promoit.wallet.cli.command;

import ru.mephi.promoit.wallet.domain.model.TransactionType;

public record CategoryReportCommand(TransactionType type) implements FinanceCommand {
}
