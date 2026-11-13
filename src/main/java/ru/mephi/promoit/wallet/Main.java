
package ru.mephi.promoit.wallet;

import ru.mephi.promoit.wallet.cli.ConsoleApplication;
import ru.mephi.promoit.wallet.cli.executor.CommandExecutor;
import ru.mephi.promoit.wallet.cli.parser.CommandParser;
import ru.mephi.promoit.wallet.domain.service.AuthService;
import ru.mephi.promoit.wallet.domain.service.UserFinanceService;
import ru.mephi.promoit.wallet.persistence.JsonPersistenceService;
import ru.mephi.promoit.wallet.persistence.LoadedState;
import ru.mephi.promoit.wallet.repository.UserFinanceRepository;
import ru.mephi.promoit.wallet.repository.UserRepository;
import ru.mephi.promoit.wallet.security.PasswordHasher;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        JsonPersistenceService persistence = new JsonPersistenceService(
            Path.of("wallet-data.json")
        );

        try {
            LoadedState state = persistence.load();

            UserRepository users = state.users();
            UserFinanceRepository finances = state.finances();

            AuthService authService = new AuthService(
                users,
                new PasswordHasher()
            );

            UserFinanceService userFinanceService = new UserFinanceService(
                authService,
                finances
            );

            CommandExecutor executor = new CommandExecutor(
                authService,
                userFinanceService
            );

            ConsoleApplication application = new ConsoleApplication(
                new CommandParser(),
                executor
            );

            try {
                application.run();
            } finally {
                persistence.save(users, finances);
            }

        } catch (IOException e) {
            System.err.println(
                "Failed to load or save application data: " + e.getMessage()
            );
            System.exit(1);
        }
    }
}
