package ru.mephi.promoit.wallet;

import ru.mephi.promoit.wallet.cli.ConsoleApplication;
import ru.mephi.promoit.wallet.cli.executor.CommandExecutor;
import ru.mephi.promoit.wallet.cli.parser.CommandParser;
import ru.mephi.promoit.wallet.domain.service.AuthService;
import ru.mephi.promoit.wallet.domain.service.UserFinanceService;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

public class Main {

    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        UserFinanceRepository financeRepository = new UserFinanceRepository();

        AuthService authService = new AuthService(
            userRepository,
            new PasswordHasher()
        );

        UserFinanceService userFinanceService = new UserFinanceService(
            authService,
            financeRepository
        );

        CommandParser parser = new CommandParser();

        CommandExecutor executor = new CommandExecutor(
            authService,
            userFinanceService
        );

        new ConsoleApplication(parser, executor).run();
    }
}
