package ru.mephi.promoit.wallet;

import ru.mephi.promoit.wallet.cli.executor.CommandExecutor;
import ru.mephi.promoit.wallet.cli.parser.CommandParser;
import ru.mephi.promoit.wallet.cli.ConsoleApplication;
import ru.mephi.promoit.wallet.repository.BudgetCatalog;
import ru.mephi.promoit.wallet.repository.CategoryCatalog;
import ru.mephi.promoit.wallet.domain.model.Wallet;
import ru.mephi.promoit.wallet.domain.service.BudgetService;
import ru.mephi.promoit.wallet.domain.service.FinanceService;

public class Main {

    public static void main(String[] args) {
        Wallet wallet = new Wallet();
        CategoryCatalog categories = new CategoryCatalog();
        BudgetCatalog budgets = new BudgetCatalog();

        FinanceService financeService = new FinanceService(
            wallet,
            categories
        );

        BudgetService budgetService = new BudgetService(
            budgets,
            categories,
            wallet
        );

        CommandParser parser = new CommandParser();

        CommandExecutor executor = new CommandExecutor(financeService, budgetService);

        new ConsoleApplication(parser, executor).run();
    }
}
