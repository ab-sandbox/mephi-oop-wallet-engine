package ru.mephi.promoit.wallet;

import ru.mephi.promoit.wallet.cli.CommandParser;
import ru.mephi.promoit.wallet.cli.ConsoleApplication;
import ru.mephi.promoit.wallet.model.*;
import ru.mephi.promoit.wallet.service.BudgetService;
import ru.mephi.promoit.wallet.service.FinanceService;

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

        new ConsoleApplication(
            financeService,
            new CommandParser(),
            budgetService
        ).run();
    }
}
