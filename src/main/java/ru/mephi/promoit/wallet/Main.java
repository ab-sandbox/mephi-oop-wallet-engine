package ru.mephi.promoit.wallet;

import ru.mephi.promoit.wallet.cli.CommandParser;
import ru.mephi.promoit.wallet.cli.ConsoleApplication;
import ru.mephi.promoit.wallet.model.Wallet;
import ru.mephi.promoit.wallet.service.FinanceService;

public class Main {

    public static void main(String[] args) {
        Wallet wallet = new Wallet();

        FinanceService financeService = new FinanceService(wallet);
        CommandParser parser = new CommandParser();

        new ConsoleApplication(financeService, parser).run();
    }
}
