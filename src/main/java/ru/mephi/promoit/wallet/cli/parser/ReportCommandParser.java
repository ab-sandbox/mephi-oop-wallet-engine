package ru.mephi.promoit.wallet.cli.parser;

import ru.mephi.promoit.wallet.cli.command.CategoryReportCommand;
import ru.mephi.promoit.wallet.cli.command.FinanceCommand;
import ru.mephi.promoit.wallet.domain.model.TransactionType;

import java.util.Locale;

public class ReportCommandParser implements CommandGroupParser {

    @Override
    public FinanceCommand parse(String[] parts) {
        CommandArguments.requireLength(parts, 2);

        if (!parts[0].equals("report")) {
            throw new IllegalArgumentException("Unknown command: " + parts[0]);
        }

        TransactionType type;

        try {
            type = TransactionType.valueOf(
                parts[1].toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Unknown transaction type: " + parts[1]
            );
        }

        return new CategoryReportCommand(type);
    }
}
