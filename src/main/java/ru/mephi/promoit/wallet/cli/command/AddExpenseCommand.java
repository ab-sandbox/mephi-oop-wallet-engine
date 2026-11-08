package ru.mephi.promoit.wallet.cli.command;

import java.math.BigDecimal;

public record AddExpenseCommand(String category, BigDecimal amount) implements FinanceCommand {
}
