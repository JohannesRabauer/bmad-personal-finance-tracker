package com.bmad.finance;

/**
 * Whether a transaction brings money in (income) or out (spending).
 */
public enum TransactionType {
    IN("In"),
    OUT("Out");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
