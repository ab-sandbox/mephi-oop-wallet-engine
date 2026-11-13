package ru.mephi.promoit.wallet.cli.executor;

import ru.mephi.promoit.wallet.cli.command.AddCategoryCommand;
import ru.mephi.promoit.wallet.cli.command.AddExpenseCommand;
import ru.mephi.promoit.wallet.cli.command.AddIncomeCommand;
import ru.mephi.promoit.wallet.cli.command.CategoryReportCommand;
import ru.mephi.promoit.wallet.cli.command.ExitCommand;
import ru.mephi.promoit.wallet.cli.command.ExportCommand;
import ru.mephi.promoit.wallet.cli.command.FinanceCommand;
import ru.mephi.promoit.wallet.cli.command.HelpCommand;
import ru.mephi.promoit.wallet.cli.command.ListBudgetsCommand;
import ru.mephi.promoit.wallet.cli.command.ListCategoriesCommand;
import ru.mephi.promoit.wallet.cli.command.LoginCommand;
import ru.mephi.promoit.wallet.cli.command.LogoutCommand;
import ru.mephi.promoit.wallet.cli.command.RegisterCommand;
import ru.mephi.promoit.wallet.cli.command.RenameCategoryCommand;
import ru.mephi.promoit.wallet.cli.command.SetBudgetCommand;
import ru.mephi.promoit.wallet.cli.command.ShowBalanceCommand;
import ru.mephi.promoit.wallet.cli.command.SummaryCommand;
import ru.mephi.promoit.wallet.cli.command.TransferCommand;
import ru.mephi.promoit.wallet.domain.model.Budget;
import ru.mephi.promoit.wallet.domain.model.Category;
import ru.mephi.promoit.wallet.domain.model.CategoryReport;
import ru.mephi.promoit.wallet.domain.model.CategoryType;
import ru.mephi.promoit.wallet.domain.model.FinanceSummary;
import ru.mephi.promoit.wallet.domain.model.User;
import ru.mephi.promoit.wallet.domain.model.UserFinance;
import ru.mephi.promoit.wallet.domain.service.AuthService;
import ru.mephi.promoit.wallet.domain.service.BudgetService;
import ru.mephi.promoit.wallet.domain.service.FinanceService;
import ru.mephi.promoit.wallet.domain.service.StatisticsService;
import ru.mephi.promoit.wallet.domain.service.TransferService;
import ru.mephi.promoit.wallet.domain.service.UserFinanceService;
import ru.mephi.promoit.wallet.persistence.CsvExportService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class CommandExecutor {

    private final AuthService authService;
    private final UserFinanceService userFinanceService;
    private final TransferService transferService;
    private final CsvExportService csvExportService;

    public CommandExecutor(
        AuthService authService,
        UserFinanceService userFinanceService,
        TransferService transferService,
        CsvExportService csvExportService
    ) {
        this.authService = authService;
        this.userFinanceService = userFinanceService;
        this.transferService = transferService;
        this.csvExportService = csvExportService;
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

                int balanceSign = financeService.getBalance().signum();
                if (balanceSign < 0) {
                    result.append("\n")
                        .append("Warning: Total expenses exceed total income!");
                } else if (balanceSign == 0) {
                    result.append("\n")
                        .append("Warning: Balance is zero!");
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
                List<CategoryReport> categories = statisticsService()
                    .getCategoryReport(report.type(), report.categories());

                if (categories.isEmpty()) {
                    yield "No transactions found";
                }

                StringBuilder result = new StringBuilder();

                result.append(
                    "%-20s %s%n".formatted("Category", "Amount")
                );

                BigDecimal total = BigDecimal.ZERO;

                for (CategoryReport category : categories) {
                    result.append(
                        "%-20s %s%n".formatted(
                            category.category(),
                            category.amount()
                        )
                    );

                    total = total.add(category.amount());
                }

                result.append(
                    "%-20s %s".formatted("Total", total)
                );

                yield result.toString();
            }

            case TransferCommand transfer -> {
                transferService.transfer(
                    transfer.recipient(),
                    transfer.amount()
                );
                yield "Transferred " + transfer.amount()
                      + " to " + transfer.recipient();
            }

            case ExportCommand export -> {
                try {
                    int count = csvExportService.export(
                        userFinanceService.current(),
                        export.path()
                    );

                    yield "Exported " + count
                          + " transactions to " + export.path();
                } catch (IOException e) {
                    yield "Failed to export transactions: " + e.getMessage();
                }
            }

            case RenameCategoryCommand rename -> {
                Category renamed = financeService().renameCategory(
                    rename.currentName(),
                    rename.type(),
                    rename.newName()
                );

                yield "Category renamed: "
                      + rename.currentName()
                      + " -> "
                      + renamed.name();
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
                  category rename <income|expense> <old-name> <new-name>
                  income add <category> <amount>
                  expense add <category> <amount>
                  transfer <recipient> <amount>
                  export csv <file>
                  budget set <category> <limit>
                  budget list
                  balance
                  summary
                  report income
                  report expense
                  report income [categories...]
                  report expense [categories...]
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
