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
}
