package ru.mephi.promoit.wallet.cli.command;

import java.math.BigDecimal;

public record SetBudgetCommand(String category, BigDecimal limit) implements FinanceCommand {
}
