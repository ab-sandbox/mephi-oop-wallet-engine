package ru.mephi.promoit.wallet.cli.executor;

import ru.mephi.promoit.wallet.cli.command.*;
import ru.mephi.promoit.wallet.domain.model.*;
import ru.mephi.promoit.wallet.domain.service.*;

import java.util.Comparator;
import java.util.List;

public class CommandExecutor {

    private final AuthService authService;
    private final UserFinanceService userFinanceService;

    public CommandExecutor(
        AuthService authService,
        UserFinanceService userFinanceService
    ) {
        this.authService = authService;
        this.userFinanceService = userFinanceService;
    }

    public String execute(FinanceCommand command) {
        checkAccess(command);

        return switch (command) {

            case AddIncomeCommand income -> {
                FinanceService financeService = financeService();

                financeService.addIncome(income.category(), income.amount());
                yield "Income added: " + income.amount();
            }

            case AddExpenseCommand expense -> {
                FinanceService financeService = financeService();
                BudgetService budgetService = budgetService();

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
                FinanceService financeService = financeService();

                Category created = financeService.addCategory(
                    category.name(),
                    category.type()
                );

                yield "Category created: "
                      + created.name() + " (" + created.type() + ")";
            }

            case ListCategoriesCommand ignored -> {
                FinanceService financeService = financeService();

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

            case ShowBalanceCommand ignored -> {
                FinanceService financeService = financeService();

                yield "Current balance: " + financeService.getBalance();
            }

            case SetBudgetCommand budget -> {
                BudgetService budgetService = budgetService();

                budgetService.setBudget(budget.category(), budget.limit());

                yield "Budget set: " + budget.category()
                      + " = " + budget.limit();
            }

            case ListBudgetsCommand ignored -> {
                BudgetService budgetService = budgetService();

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

            case SummaryCommand ignored -> {
                FinanceSummary summary = statisticsService().getSummary();

                yield """
                    Income:   %s
                    Expenses: %s
                    Balance:  %s
                    """.formatted(
                    summary.income(),
                    summary.expenses(),
                    summary.balance()
                ).stripTrailing();
            }

            case CategoryReportCommand report -> {
                List<CategoryReport> categories =
                    statisticsService().getCategoryReport(report.type());

                if (categories.isEmpty()) {
                    yield "No transactions found";
                }

                StringBuilder result = new StringBuilder();

                result.append(
                    "%-20s %s%n".formatted("Category", "Amount")
                );

                for (CategoryReport category : categories) {
                    result.append(
                        "%-20s %s%n".formatted(
                            category.category(),
                            category.amount()
                        )
                    );
                }

                yield result.toString().stripTrailing();
            }

            case RegisterCommand register -> {
                User user = authService.register(
                    register.login(),
                    register.password()
                );

                yield "User registered: " + user.login();
            }

            case LoginCommand login -> {
                User user = authService.login(
                    login.login(),
                    login.password()
                );

                yield "Logged in as " + user.login();
            }

            case LogoutCommand ignored -> {
                authService.requireCurrentUser();
                authService.logout();
                yield "Logged out";
            }

            case HelpCommand ignored -> """
                Available commands:
                  register <login> <password>
                  login <login> <password>
                  logout
                  category add <income|expense> <name>
                  category list
                  income add <category> <amount>
                  expense add <category> <amount>
                  budget set <category> <limit>
                  budget list
                  balance
                  summary
                  report income
                  report expense
                  help
                  exit
                """.stripTrailing();

            case ExitCommand ignored -> "Goodbye!";
        };
    }

    private void checkAccess(FinanceCommand command) {
        if (command instanceof RegisterCommand
            || command instanceof LoginCommand
            || command instanceof LogoutCommand
            || command instanceof HelpCommand
            || command instanceof ExitCommand) {
            return;
        }

        authService.requireCurrentUser();
    }

    private FinanceService financeService() {
        UserFinance finance = userFinanceService.current();

        return new FinanceService(
            finance.wallet(),
            finance.categories()
        );
    }

    private BudgetService budgetService() {
        UserFinance finance = userFinanceService.current();

        return new BudgetService(
            finance.budgets(),
            finance.categories(),
            finance.wallet()
        );
    }

    private StatisticsService statisticsService() {
        UserFinance finance = userFinanceService.current();

        return new StatisticsService(
            finance.wallet(),
            finance.categories()
        );
    }
}
