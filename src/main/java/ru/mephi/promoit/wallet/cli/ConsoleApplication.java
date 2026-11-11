
package ru.mephi.promoit.wallet.cli;

import ru.mephi.promoit.wallet.cli.command.ExitCommand;
import ru.mephi.promoit.wallet.cli.command.FinanceCommand;
import ru.mephi.promoit.wallet.cli.executor.CommandExecutor;
import ru.mephi.promoit.wallet.cli.parser.CommandParser;

import java.util.Scanner;

public class ConsoleApplication {

    private final CommandParser parser;
    private final CommandExecutor executor;

    public ConsoleApplication(
        CommandParser parser,
        CommandExecutor executor
    ) {
        this.parser = parser;
        this.executor = executor;
    }

    public void run() {
        System.out.println("=== Personal Finance Manager ===");
        System.out.println("Type 'help' for available commands.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nfinance> ");

                if (!scanner.hasNextLine()) {
                    break;
                }

                try {
                    FinanceCommand command = parser.parse(scanner.nextLine());

                    System.out.println(executor.execute(command));

                    if (command instanceof ExitCommand) {
                        break;
                    }

                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }
}
