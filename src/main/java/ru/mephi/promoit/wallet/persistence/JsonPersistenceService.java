package ru.mephi.promoit.wallet.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.mephi.promoit.wallet.domain.model.*;
import ru.mephi.promoit.wallet.persistence.dto.*;
import ru.mephi.promoit.wallet.repository.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class JsonPersistenceService {

    private final Path path;
    private final ObjectMapper mapper;

    public JsonPersistenceService(Path path) {
        this.path = path;
        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    }

    public void save(
        UserRepository users,
        UserFinanceRepository finances
    ) throws IOException {
        ApplicationData data = collectData(users, finances);
        writeAtomically(data);
    }

    public LoadedState load() throws IOException {
        UserRepository users = new UserRepository();
        UserFinanceRepository finances = new UserFinanceRepository();

        if (Files.notExists(path)) {
            return new LoadedState(users, finances);
        }

        ApplicationData data = readData();

        try {
            restoreUsers(data.users(), users);
            restoreFinances(data.finances(), users, finances);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new IOException("Invalid application data", e);
        }

        return new LoadedState(users, finances);
    }

    private ApplicationData collectData(
        UserRepository users,
        UserFinanceRepository finances
    ) {
        List<UserData> userData = users.findAll().stream()
            .map(user -> new UserData(
                user.id(),
                user.login(),
                user.passwordHash()
            ))
            .toList();

        List<UserFinanceData> financeData = new ArrayList<>();

        for (Map.Entry<UUID, UserFinance> entry
            : finances.findAll().entrySet()) {

            UserFinance finance = entry.getValue();

            financeData.add(new UserFinanceData(
                entry.getKey(),
                finance.wallet().getTransactions(),
                finance.categories().findAll(),
                finance.budgets().findAll()
            ));
        }

        return new ApplicationData(userData, financeData);
    }

    private void writeAtomically(ApplicationData data) throws IOException {
        Path target = path.toAbsolutePath();
        Path parent = target.getParent();

        Files.createDirectories(parent);

        Path temporary = Files.createTempFile(
            parent,
            "wallet-",
            ".tmp"
        );

        try {
            mapper.writerWithDefaultPrettyPrinter()
                .writeValue(temporary.toFile(), data);

            moveToTarget(temporary, target);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void moveToTarget(Path temporary, Path target)
        throws IOException {
        try {
            Files.move(
                temporary,
                target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                temporary,
                target,
                StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private ApplicationData readData() throws IOException {
        ApplicationData data = mapper.readValue(
            path.toFile(),
            ApplicationData.class
        );

        if (data == null
            || data.users() == null
            || data.finances() == null) {
            throw new IOException("Invalid application data");
        }

        return data;
    }

    private void restoreUsers(
        List<UserData> data,
        UserRepository users
    ) {
        Set<UUID> userIds = new HashSet<>();

        for (UserData user : data) {
            if (user == null) {
                throw new IllegalArgumentException("Invalid user");
            }

            User restored = new User(
                user.id(),
                user.login(),
                user.passwordHash()
            );

            if (!userIds.add(restored.id())) {
                throw new IllegalArgumentException(
                    "Duplicate user ID"
                );
            }

            users.save(restored);
        }
    }

    private void restoreFinances(
        List<UserFinanceData> data,
        UserRepository users,
        UserFinanceRepository finances
    ) {
        Set<UUID> userIds = new HashSet<>();

        for (User user : users.findAll()) {
            userIds.add(user.id());
        }

        for (UserFinanceData item : data) {
            validateFinanceData(item, userIds);

            UserFinance finance = new UserFinance();

            restoreCategories(item.categories(), finance);

            Set<UUID> categoryIds = getCategoryIds(finance);

            restoreTransactions(
                item.transactions(),
                categoryIds,
                finance
            );

            restoreBudgets(
                item.budgets(),
                categoryIds,
                finance
            );

            finances.restore(item.userId(), finance);
        }
    }

    private void validateFinanceData(
        UserFinanceData data,
        Set<UUID> userIds
    ) {
        if (data == null
            || !userIds.contains(data.userId())
            || data.categories() == null
            || data.transactions() == null
            || data.budgets() == null) {
            throw new IllegalArgumentException(
                "Invalid user finances"
            );
        }
    }

    private void restoreCategories(
        List<Category> categories,
        UserFinance finance
    ) {
        for (Category category : categories) {
            finance.categories().restore(category);
        }
    }

    private Set<UUID> getCategoryIds(UserFinance finance) {
        Set<UUID> categoryIds = new HashSet<>();

        for (Category category : finance.categories().findAll()) {
            categoryIds.add(category.id());
        }

        return categoryIds;
    }

    private void restoreTransactions(
        List<Transaction> transactions,
        Set<UUID> categoryIds,
        UserFinance finance
    ) {
        Set<UUID> transactionIds = new HashSet<>();

        for (Transaction transaction : transactions) {
            if (transaction == null
                || !categoryIds.contains(transaction.categoryId())
                || !transactionIds.add(transaction.id())) {
                throw new IllegalArgumentException(
                    "Invalid transaction"
                );
            }

            finance.wallet().addTransaction(transaction);
        }
    }

    private void restoreBudgets(
        List<Budget> budgets,
        Set<UUID> categoryIds,
        UserFinance finance
    ) {
        for (Budget budget : budgets) {
            if (budget == null
                || !categoryIds.contains(budget.categoryId())
                || finance.budgets()
                       .findByCategory(budget.categoryId()) != null) {
                throw new IllegalArgumentException(
                    "Invalid budget"
                );
            }

            finance.budgets().set(budget);
        }
    }
}
