package ru.mephi.promoit.wallet.cli;

import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.cli.command.*;
import ru.mephi.promoit.wallet.cli.parser.CommandParser;
import ru.mephi.promoit.wallet.domain.model.CategoryType;

import java.math.BigDecimal;

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
}
