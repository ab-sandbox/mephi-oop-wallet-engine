package ru.mephi.promoit.wallet.cli.command;

import java.math.BigDecimal;

public record TransferCommand(
    String recipient,
    BigDecimal amount
) implements FinanceCommand {
}
