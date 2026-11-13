package ru.mephi.promoit.wallet.cli.parser;

import ru.mephi.promoit.wallet.cli.command.ExportCommand;
import ru.mephi.promoit.wallet.cli.command.FinanceCommand;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class ExportCommandParser implements CommandGroupParser {

    @Override
    public FinanceCommand parse(String[] parts) {
        CommandArguments.requireLength(parts, 3);

        if (!"csv".equals(parts[1])) {
            throw new IllegalArgumentException(
                "Unsupported export format: " + parts[1]
            );
        }

        try {
            return new ExportCommand(Path.of(parts[2]));
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException(
                "Invalid export path: " + parts[2], e
            );
        }
    }
}
