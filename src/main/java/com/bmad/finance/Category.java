package com.bmad.finance;

/**
 * Fixed, predefined set of transaction categories (v1 is not user-managed).
 * Covers both spending and income purposes to keep the demo small.
 */
public enum Category {
    GROCERIES("Groceries"),
    DINING("Dining"),
    TRANSPORT("Transport"),
    UTILITIES("Utilities"),
    RENT("Rent"),
    HEALTH("Health"),
    ENTERTAINMENT("Entertainment"),
    SALARY("Salary"),
    GIFT("Gift"),
    OTHER("Other");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
