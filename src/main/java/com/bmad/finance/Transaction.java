package com.bmad.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * A single income (IN) or spending (OUT) entry, owned by the username that
 * created it. Users only ever see and act on their own rows.
 */
@Entity
@Table(name = "transactions")
public class Transaction extends PanacheEntity {

    @Column(nullable = false)
    public String owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    public BigDecimal amount;

    @Column(nullable = false)
    public LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Category category;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    public Transaction() {
        // required by JPA
    }

    public Transaction(String owner, TransactionType type, BigDecimal amount,
                       LocalDate date, Category category) {
        this.owner = owner;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * All transactions owned by the given user, most recent first.
     */
    public static List<Transaction> listForOwner(String owner) {
        return list("owner = ?1 order by date desc, id desc", owner);
    }
}
