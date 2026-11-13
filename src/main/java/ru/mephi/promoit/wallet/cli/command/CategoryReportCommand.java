package ru.mephi.promoit.wallet.cli.command;

import ru.mephi.promoit.wallet.domain.model.TransactionType;

import java.util.List;

public record CategoryReportCommand(TransactionType type, List<String> categories) implements FinanceCommand {

    public CategoryReportCommand {
        categories = List.copyOf(categories);
    }

    public CategoryReportCommand(TransactionType type) {
        this(type, List.of());
    }
}
