package ru.mephi.promoit.wallet.cli.parser;

import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.cli.command.*;
import ru.mephi.promoit.wallet.domain.model.CategoryType;
import ru.mephi.promoit.wallet.domain.model.TransactionType;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    private final CommandParser parser = new CommandParser();

    @Test
    void shouldParseIncome() {
        FinanceCommand command = parser.parse("income add Salary 50000");

        assertEquals(
            new AddIncomeCommand("Salary", new BigDecimal("50000")),
            command
        );
    }

    @Test
    void shouldParseExpense() {
        FinanceCommand command = parser.parse("expense add Food 1500");

        assertEquals(
            new AddExpenseCommand("Food", new BigDecimal("1500")),
            command
        );
    }

    @Test
    void shouldParseCategory() {
        FinanceCommand command = parser.parse("category add expense Food");

        assertEquals(
            new AddCategoryCommand("Food", CategoryType.EXPENSE),
            command
        );
    }

    @Test
    void shouldParseBudget() {
        FinanceCommand command = parser.parse("budget set Food 4000");

        assertEquals(
            new SetBudgetCommand("Food", new BigDecimal("4000")),
            command
        );
    }

    @Test
    void shouldRejectInvalidAmount() {
        assertThrows(IllegalArgumentException.class,
            () -> parser.parse("expense add Food abc"));
    }

    @Test
    void shouldRejectUnknownCommand() {
        assertThrows(IllegalArgumentException.class,
            () -> parser.parse("unknown command"));
    }

    @Test
    void shouldRejectMissingArguments() {
        assertThrows(IllegalArgumentException.class,
            () -> parser.parse("budget set Food"));
    }

    @Test
    void shouldParseBalance() {
        assertInstanceOf(
            ShowBalanceCommand.class,
            parser.parse("balance")
        );
    }

    @Test
    void shouldParseSummary() {
        assertEquals(
            new SummaryCommand(),
            parser.parse("summary")
        );
    }

    @Test
    void shouldParseIncomeReport() {
        assertEquals(
            new CategoryReportCommand(TransactionType.INCOME),
            parser.parse("report income")
        );
    }

    @Test
    void shouldParseExpenseReport() {
        assertEquals(
            new CategoryReportCommand(TransactionType.EXPENSE),
            parser.parse("report expense")
        );
    }

    @Test
    void shouldRejectUnknownReportType() {
        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse("report unknown")
        );
    }

    @Test
    void shouldRejectReportWithoutType() {
        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse("report")
        );
    }

    @Test
    void shouldParseReportWithoutCategories() {
        CommandParser parser = new CommandParser();

        CategoryReportCommand command = assertInstanceOf(
            CategoryReportCommand.class,
            parser.parse("report expense")
        );

        assertEquals(TransactionType.EXPENSE, command.type());
        assertTrue(command.categories().isEmpty());
    }

    @Test
    void shouldParseReportWithSelectedCategories() {
        CommandParser parser = new CommandParser();

        CategoryReportCommand command = assertInstanceOf(
            CategoryReportCommand.class,
            parser.parse("report expense Food Taxi")
        );

        assertEquals(TransactionType.EXPENSE, command.type());
        assertEquals(List.of("Food", "Taxi"), command.categories());
    }
}
