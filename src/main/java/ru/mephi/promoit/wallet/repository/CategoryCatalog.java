package ru.mephi.promoit.wallet.repository;

import ru.mephi.promoit.wallet.domain.model.Category;
import ru.mephi.promoit.wallet.domain.model.CategoryType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class CategoryCatalog {

    private final Map<UUID, Category> categories = new HashMap<>();

    public Category add(String name, CategoryType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank");
        }

        if (type == null) {
            throw new IllegalArgumentException("Category type must not be null");
        }

        String normalizedName = name.trim();

        boolean exists = categories.values().stream()
            .anyMatch(category ->
                category.type() == type
                && category.name().equalsIgnoreCase(normalizedName)
            );

        if (exists) {
            throw new IllegalArgumentException("Category already exists");
        }

        Category category = new Category(
            UUID.randomUUID(),
            normalizedName,
            type
        );

        categories.put(category.id(), category);
        return category;
    }

    public Category findByName(String name, CategoryType type) {
        if (name == null || name.isBlank() || type == null) {
            throw new IllegalArgumentException("Invalid category");
        }

        return categories.values().stream()
            .filter(category -> category.type() == type)
            .filter(category -> category.name().equalsIgnoreCase(name.trim()))
            .findFirst()
            .orElseThrow(() ->
                new IllegalArgumentException("Category not found: " + name)
            );
    }

    public List<Category> findAll() {
        return new ArrayList<>(categories.values());
    }

    public void restore(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }

        boolean exists = categories.containsKey(category.id())
                         || categories.values().stream()
                             .anyMatch(existing ->
                                 existing.type() == category.type()
                                 && existing.name().equalsIgnoreCase(category.name())
                             );

        if (exists) {
            throw new IllegalArgumentException("Category already exists");
        }

        categories.put(category.id(), category);
    }
}
