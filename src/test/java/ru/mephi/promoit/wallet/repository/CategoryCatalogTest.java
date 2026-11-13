package ru.mephi.promoit.wallet.repository;

import org.junit.jupiter.api.Test;
import ru.mephi.promoit.wallet.domain.model.Category;
import ru.mephi.promoit.wallet.domain.model.CategoryType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CategoryCatalogTest {

    @Test
    void shouldRestoreCategoryWithOriginalId() {
        CategoryCatalog catalog = new CategoryCatalog();

        UUID id = UUID.randomUUID();
        Category category = new Category(
            id,
            "Food",
            CategoryType.EXPENSE
        );

        catalog.restore(category);

        assertEquals(1, catalog.findAll().size());
        assertEquals(category, catalog.findByName("Food", CategoryType.EXPENSE));
        assertEquals(id, catalog.findAll().getFirst().id());
    }

    @Test
    void shouldRejectDuplicateCategoryId() {
        CategoryCatalog catalog = new CategoryCatalog();

        Category category = new Category(
            UUID.randomUUID(),
            "Food",
            CategoryType.EXPENSE
        );

        catalog.restore(category);

        assertThrows(
            IllegalArgumentException.class,
            () -> catalog.restore(category)
        );
    }

    @Test
    void shouldRejectDuplicateCategoryName() {
        CategoryCatalog catalog = new CategoryCatalog();

        catalog.add("Food", CategoryType.EXPENSE);

        Category duplicate = new Category(
            UUID.randomUUID(),
            "food",
            CategoryType.EXPENSE
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> catalog.restore(duplicate)
        );
    }

    @Test
    void shouldRenameCategoryPreservingId() {
        CategoryCatalog catalog = new CategoryCatalog();

        Category original = catalog.add("Food", CategoryType.EXPENSE);

        Category renamed = catalog.rename(
            "Food",
            CategoryType.EXPENSE,
            "Groceries"
        );

        assertEquals(original.id(), renamed.id());
        assertEquals(CategoryType.EXPENSE, renamed.type());
        assertEquals("Groceries", renamed.name());

        assertEquals(
            renamed,
            catalog.findByName("Groceries", CategoryType.EXPENSE)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> catalog.findByName("Food", CategoryType.EXPENSE)
        );
    }

    @Test
    void shouldRejectDuplicateNameOnRename() {
        CategoryCatalog catalog = new CategoryCatalog();

        catalog.add("Food", CategoryType.EXPENSE);
        catalog.add("Transport", CategoryType.EXPENSE);

        assertThrows(
            IllegalArgumentException.class,
            () -> catalog.rename(
                "Food",
                CategoryType.EXPENSE,
                "Transport"
            )
        );
    }

    @Test
    void shouldAllowRenamingToSameNameWithDifferentCase() {
        CategoryCatalog catalog = new CategoryCatalog();

        Category original = catalog.add("Food", CategoryType.EXPENSE);

        Category renamed = catalog.rename(
            "Food",
            CategoryType.EXPENSE,
            "food"
        );

        assertEquals(original.id(), renamed.id());
        assertEquals("food", renamed.name());
    }
}
