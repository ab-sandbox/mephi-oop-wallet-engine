package ru.mephi.promoit.wallet.cli;

import ru.mephi.promoit.wallet.cli.command.*;
import ru.mephi.promoit.wallet.model.Budget;
import ru.mephi.promoit.wallet.model.Category;
import ru.mephi.promoit.wallet.model.CategoryType;
import ru.mephi.promoit.wallet.service.BudgetService;
import ru.mephi.promoit.wallet.service.FinanceService;

import java.util.Comparator;
import java.util.Scanner;

public class ConsoleApplication {

    private final FinanceService financeService;
    private final CommandParser parser;
    private final BudgetService budgetService;

    public ConsoleApplication(
        FinanceService financeService,
        CommandParser parser,
        BudgetService budgetService
    ) {
        this.financeService = financeService;
        this.parser = parser;
        this.budgetService = budgetService;
    }

    public void run() {
        System.out.println("=== Personal Finance Manager ===");
        System.out.println("Type 'help' for available commands.");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                System.out.print("\nfinance> ");

                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine();

                try {
                    FinanceCommand command = parser.parse(input);

                    switch (command) {
                        case AddIncomeCommand income -> {
                            financeService.addIncome(
                                income.category(),
                                income.amount()
                            );
                            System.out.println("Income added: " + income.amount());
                        }

                        case AddExpenseCommand expense -> {
                            financeService.addExpense(
                                expense.category(),
                                expense.amount()
                            );

                            System.out.println("Expense added: " + expense.amount());

                            String warning = budgetService.getWarning(expense.category());

                            if (warning != null) {
                                System.out.println(warning);
                            }

                            if (financeService.getBalance().signum() < 0) {
                                System.out.println(
                                    "Warning: Total expenses exceed total income!"
                                );
                            }
                        }

                        case AddCategoryCommand category -> {
                            Category created = financeService.addCategory(
                                category.name(),
                                category.type()
                            );
                            System.out.println("Category created: "
                                               + created.name() + " (" + created.type() + ")");
                        }

                        case ListCategoriesCommand ignored -> {
                            for (CategoryType type : CategoryType.values()) {
                                System.out.println(type + ":");

                                financeService.getCategories().stream()
                                    .filter(category -> category.type() == type)
                                    .sorted(Comparator.comparing(Category::name))
                                    .forEach(category ->
                                        System.out.println("  " + category.name())
                                    );
                            }
                        }

                        case ShowBalanceCommand ignored -> System.out.println(
                            "Current balance: " + financeService.getBalance()
                        );

                        case SetBudgetCommand budget -> {
                            budgetService.setBudget(
                                budget.category(),
                                budget.limit()
                            );

                            System.out.println(
                                "Budget set: " + budget.category()
                                + " = " + budget.limit()
                            );
                        }

                        case ListBudgetsCommand ignored -> {
                            System.out.printf(
                                "%-18s %12s %12s %12s%n",
                                "Category", "Limit", "Spent", "Remaining"
                            );

                            for (Budget budget : budgetService.getBudgets()) {
                                Category category = budgetService.getCategory(budget);

                                System.out.printf(
                                    "%-18s %12s %12s %12s%n",
                                    category.name(),
                                    budget.limit(),
                                    budgetService.getSpent(budget),
                                    budgetService.getRemaining(budget)
                                );
                            }
                        }

                        case HelpCommand ignored -> printHelp();

                        case ExitCommand ignored -> {
                            System.out.println("Goodbye!");
                            running = false;
                        }
                    }

                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private void printHelp() {
        System.out.println("""
            Available commands:
              category add <income|expense> <name>
              category list
              income add <category> <amount>
              expense add <category> <amount>
              budget set <category> <limit>
              budget list
              balance
              help
              exit
            """);
    }
}
