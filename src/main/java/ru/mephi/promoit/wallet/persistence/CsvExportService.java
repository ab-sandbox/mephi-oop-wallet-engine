package ru.mephi.promoit.wallet.persistence;

import ru.mephi.promoit.wallet.domain.model.Category;
import ru.mephi.promoit.wallet.domain.model.Transaction;
import ru.mephi.promoit.wallet.domain.model.UserFinance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CsvExportService {

    public int export(UserFinance finance, Path path) throws IOException {
        Map<UUID, String> categories = new HashMap<>();

        for (Category category : finance.categories().findAll()) {
            categories.put(category.id(), category.name());
        }

        List<String> lines = new ArrayList<>();

        lines.add("id,type,category,amount,createdAt,transferId");

        for (Transaction transaction : finance.wallet().getTransactions()) {
            String category = categories.get(transaction.categoryId());

            if (category == null) {
                throw new IllegalStateException(
                    "Category not found: " + transaction.categoryId()
                );
            }

            lines.add(String.join(",",
                transaction.id().toString(),
                transaction.type().name(),
                escape(category),
                transaction.amount().toPlainString(),
                transaction.createdAt().toString(),
                transaction.transferId() == null
                    ? ""
                    : transaction.transferId().toString()
            ));
        }

        Files.write(path, lines, StandardCharsets.UTF_8);

        return lines.size() - 1;
    }

    private String escape(String value) {
        if (value.contains(",")
            || value.contains("\"")
            || value.contains("\n")
            || value.contains("\r")) {

            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
