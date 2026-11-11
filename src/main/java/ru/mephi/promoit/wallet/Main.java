package ru.mephi.promoit.wallet;

import ru.mephi.promoit.wallet.cli.ConsoleApplication;
import ru.mephi.promoit.wallet.cli.executor.CommandExecutor;
import ru.mephi.promoit.wallet.cli.parser.CommandParser;
import ru.mephi.promoit.wallet.domain.model.Wallet;
import ru.mephi.promoit.wallet.domain.service.AuthService;
import ru.mephi.promoit.wallet.domain.service.BudgetService;
import ru.mephi.promoit.wallet.domain.service.FinanceService;
import ru.mephi.promoit.wallet.repository.BudgetCatalog;
import ru.mephi.promoit.wallet.repository.CategoryCatalog;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

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

        UserRepository userRepository = new UserRepository();

        AuthService authService = new AuthService(
            userRepository,
            new PasswordHasher()
        );

        CommandExecutor executor = new CommandExecutor(
            financeService,
            budgetService,
            authService
        );

        new ConsoleApplication(parser, executor).run();
    }
}
