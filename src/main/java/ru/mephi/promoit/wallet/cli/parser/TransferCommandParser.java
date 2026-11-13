package ru.mephi.promoit.wallet.cli.parser;

import ru.mephi.promoit.wallet.cli.command.FinanceCommand;
import ru.mephi.promoit.wallet.cli.command.TransferCommand;

public class TransferCommandParser implements CommandGroupParser {

    @Override
    public FinanceCommand parse(String[] parts) {
        CommandArguments.requireLength(parts, 3);

        return new TransferCommand(
            parts[1],
            CommandArguments.parseAmount(parts[2])
        );
    }
}
