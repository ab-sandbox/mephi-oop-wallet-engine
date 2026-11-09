package ru.mephi.promoit.wallet.cli.parser;

import ru.mephi.promoit.wallet.cli.command.FinanceCommand;

public interface CommandGroupParser {

    FinanceCommand parse(String[] parts);
}
