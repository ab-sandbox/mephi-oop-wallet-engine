package ru.mephi.promoit.wallet.cli.parser;

import ru.mephi.promoit.wallet.cli.command.CategoryReportCommand;
import ru.mephi.promoit.wallet.cli.command.FinanceCommand;
import ru.mephi.promoit.wallet.domain.model.TransactionType;

import java.util.Arrays;
import java.util.List;

public class ReportCommandParser implements CommandGroupParser {

    @Override
    public FinanceCommand parse(String[] parts) {
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                "Usage: report <income|expense> [categories...]"
            );
        }

        TransactionType type = switch (parts[1]) {
            case "income" -> TransactionType.INCOME;
            case "expense" -> TransactionType.EXPENSE;
            default -> throw new IllegalArgumentException(
                "Unknown report type: " + parts[1]
            );
        };

        List<String> categories = Arrays.asList(parts)
            .subList(2, parts.length);

        return new CategoryReportCommand(type, categories);
    }
}
