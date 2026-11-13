package org.example.model;

public enum Role {
    SELLER("Продавец"),
    TRADE_MANAGER("Менеджер торговли"),
    ADMIN("Администратор");

    private final String title;

    Role(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
