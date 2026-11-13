package ru.mephi.promoit.wallet.domain.model;

public enum SystemCategory {

    TRANSFER_OUT("TransferOut", CategoryType.EXPENSE),
    TRANSFER_IN("TransferIn", CategoryType.INCOME);

    private final String name;
    private final CategoryType type;

    SystemCategory(String name, CategoryType type) {
        this.name = name;
        this.type = type;
    }

    public String categoryName() {
        return name;
    }

    public CategoryType categoryType() {
        return type;
    }
}
