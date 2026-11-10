package ru.mephi.promoit.wallet.cli.executor;

import ru.mephi.promoit.wallet.cli.command.*;
import ru.mephi.promoit.wallet.domain.model.Budget;
import ru.mephi.promoit.wallet.domain.model.Category;
import ru.mephi.promoit.wallet.domain.model.CategoryType;
import ru.mephi.promoit.wallet.domain.service.BudgetService;
import ru.mephi.promoit.wallet.domain.service.FinanceService;

import java.util.Comparator;

public class CommandExecutor {

    private final FinanceService financeService;
    private final BudgetService budgetService;

    public CommandExecutor(
        FinanceService financeService,
        BudgetService budgetService
    ) {
        this.financeService = financeService;
        this.budgetService = budgetService;
    }

    public String execute(FinanceCommand command) {
        return switch (command) {

            case AddIncomeCommand income -> {
                financeService.addIncome(income.category(), income.amount());
                yield "Income added: " + income.amount();
            }

            case AddExpenseCommand expense -> {
                financeService.addExpense(expense.category(), expense.amount());

                StringBuilder result = new StringBuilder(
                    "Expense added: " + expense.amount()
                );

                String warning = budgetService.getWarning(expense.category());

                if (warning != null) {
                    result.append("\n").append(warning);
                }

                if (financeService.getBalance().signum() < 0) {
                    result.append("\n")
                        .append("Warning: Total expenses exceed total income!");
                }

                yield result.toString();
            }

            case AddCategoryCommand category -> {
                Category created = financeService.addCategory(
                    category.name(),
                    category.type()
                );

                yield "Category created: "
                      + created.name() + " (" + created.type() + ")";
            }

            case ListCategoriesCommand ignored -> {
                StringBuilder result = new StringBuilder();

                for (CategoryType type : CategoryType.values()) {
                    result.append(type).append(":\n");

                    financeService.getCategories().stream()
                        .filter(category -> category.type() == type)
                        .sorted(Comparator.comparing(Category::name))
                        .forEach(category ->
                            result.append("  ")
                                .append(category.name())
                                .append("\n")
                        );
                }

                yield result.toString().stripTrailing();
            }

            case ShowBalanceCommand ignored -> "Current balance: " + financeService.getBalance();

            case SetBudgetCommand budget -> {
                budgetService.setBudget(budget.category(), budget.limit());

                yield "Budget set: " + budget.category()
                      + " = " + budget.limit();
            }

            case ListBudgetsCommand ignored -> {
                StringBuilder result = new StringBuilder();

                result.append(String.format(
                    "%-18s %12s %12s %12s%n",
                    "Category", "Limit", "Spent", "Remaining"
                ));

                for (Budget budget : budgetService.getBudgets()) {
                    Category category = budgetService.getCategory(budget);

                    result.append(String.format(
                        "%-18s %12s %12s %12s%n",
                        category.name(),
                        budget.limit(),
                        budgetService.getSpent(budget),
                        budgetService.getRemaining(budget)
                    ));
                }

                yield result.toString().stripTrailing();
            }

            case HelpCommand ignored -> """
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
                """.stripTrailing();

            case ExitCommand ignored -> "Goodbye!";
        };
    }
}
