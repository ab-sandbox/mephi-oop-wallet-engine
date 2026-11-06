package ru.mephi.promoit.wallet.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void shouldCreateCategory() {
        Category category = new Category(
            UUID.randomUUID(),
            "Food",
            CategoryType.EXPENSE
        );

        assertEquals("Food", category.name());
        assertEquals(CategoryType.EXPENSE, category.type());
    }

    @Test
    void shouldTrimCategoryName() {
        Category category = new Category(
            UUID.randomUUID(),
            "  Food  ",
            CategoryType.EXPENSE
        );

        assertEquals("Food", category.name());
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
            new Category(
                UUID.randomUUID(),
                "   ",
                CategoryType.EXPENSE
            )
        );
    }

    @Test
    void shouldRejectNullType() {
        assertThrows(IllegalArgumentException.class, () ->
            new Category(
                UUID.randomUUID(),
                "Food",
                null
            )
        );
    }
}
