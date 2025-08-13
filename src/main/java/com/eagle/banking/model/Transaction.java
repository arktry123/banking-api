package com.eagle.banking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull(message = "amount is required")
    @Column(nullable = false)
    private BigDecimal amount;

    @NotNull(message = "type is required")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // Store enum name in DB instead of ordinal number
    private TransactionType type;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

}
