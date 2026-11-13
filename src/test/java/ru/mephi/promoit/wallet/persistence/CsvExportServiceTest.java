package ru.mephi.promoit.wallet.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.mephi.promoit.wallet.domain.model.CategoryType;
import ru.mephi.promoit.wallet.domain.model.Transaction;
import ru.mephi.promoit.wallet.domain.model.TransactionType;
import ru.mephi.promoit.wallet.domain.model.UserFinance;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExportServiceTest {

    @TempDir
    Path tempDir;

    private final CsvExportService service = new CsvExportService();

    @Test
    void shouldExportTransactions() throws Exception {
        UserFinance finance = new UserFinance();

        var salary = finance.categories().add(
            "Salary",
            CategoryType.INCOME
        );

        var food = finance.categories().add(
            "Food",
            CategoryType.EXPENSE
        );

        finance.wallet().addTransaction(new Transaction(
            UUID.randomUUID(),
            TransactionType.INCOME,
            salary.id(),
            new BigDecimal("5000"),
            Instant.parse("2026-11-13T10:00:00Z"),
            null
        ));

        finance.wallet().addTransaction(new Transaction(
            UUID.randomUUID(),
            TransactionType.EXPENSE,
            food.id(),
            new BigDecimal("300"),
            Instant.parse("2026-11-13T10:05:00Z"),
            null
        ));

        Path file = tempDir.resolve("transactions.csv");

        int count = service.export(finance, file);

        List<String> lines = Files.readAllLines(
            file,
            StandardCharsets.UTF_8
        );

        assertEquals(2, count);
        assertEquals(3, lines.size());
        assertEquals(
            "id,type,category,amount,createdAt,transferId",
            lines.getFirst()
        );
        assertTrue(lines.get(1).contains(",INCOME,Salary,5000,"));
        assertTrue(lines.get(2).contains(",EXPENSE,Food,300,"));
    }

    @Test
    void shouldExportEmptyWallet() throws Exception {
        UserFinance finance = new UserFinance();
        Path file = tempDir.resolve("empty.csv");

        int count = service.export(finance, file);

        assertEquals(0, count);
        assertEquals(
            List.of("id,type,category,amount,createdAt,transferId"),
            Files.readAllLines(file)
        );
    }

    @Test
    void shouldEscapeCategoryName() throws Exception {
        UserFinance finance = new UserFinance();

        var category = finance.categories().add(
            "Food, \"Delivery\"",
            CategoryType.EXPENSE
        );

        finance.wallet().addTransaction(new Transaction(
            UUID.randomUUID(),
            TransactionType.EXPENSE,
            category.id(),
            new BigDecimal("450"),
            Instant.parse("2026-11-13T10:00:00Z"),
            null
        ));

        Path file = tempDir.resolve("escaped.csv");

        service.export(finance, file);

        String content = Files.readString(file);

        assertTrue(
            content.contains("\"Food, \"\"Delivery\"\"\"")
        );
    }

    @Test
    void shouldExportTransferId() throws Exception {
        UserFinance finance = new UserFinance();

        var category = finance.categories().add(
            "TransferOut",
            CategoryType.EXPENSE
        );

        UUID transferId = UUID.randomUUID();

        finance.wallet().addTransaction(new Transaction(
            UUID.randomUUID(),
            TransactionType.EXPENSE,
            category.id(),
            new BigDecimal("1500"),
            Instant.parse("2026-11-13T10:00:00Z"),
            transferId
        ));

        Path file = tempDir.resolve("transfer.csv");

        service.export(finance, file);

        String content = Files.readString(file);

        assertTrue(content.contains(transferId.toString()));
    }

    @Test
    void shouldRejectUnknownCategory() {
        UserFinance finance = new UserFinance();

        finance.wallet().addTransaction(new Transaction(
            UUID.randomUUID(),
            TransactionType.EXPENSE,
            UUID.randomUUID(),
            new BigDecimal("100"),
            Instant.now(),
            null
        ));

        Path file = tempDir.resolve("invalid.csv");

        assertThrows(
            IllegalStateException.class,
            () -> service.export(finance, file)
        );

        assertFalse(Files.exists(file));
    }
}
