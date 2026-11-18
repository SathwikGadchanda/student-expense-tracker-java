package com.studentfinance.model;

import java.util.List;

public final class Category {
    public static final List<String> EXPENSE = List.of(
            "Food", "Transportation", "Education", "Shopping", "Entertainment", "Other");
    public static final List<String> INCOME = List.of(
            "Pocket Money", "Scholarship", "Part-time Work", "Gift", "Other");

    private Category() { }

    public static List<String> forType(TransactionType type) {
        return type == TransactionType.INCOME ? INCOME : EXPENSE;
    }
}
