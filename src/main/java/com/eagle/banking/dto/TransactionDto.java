package com.eagle.banking.dto;

import com.eagle.banking.model.Transaction;
import com.eagle.banking.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDto {
    private String id = UUID.randomUUID().toString();

    private String accountType;

    private BigDecimal amount;

    private TransactionType type;

    private Instant createdAt = Instant.now();

    public static TransactionDto fromEntity(Transaction txn) {
        return new TransactionDto(txn.getId(),
                txn.getAccount().getAccountType(),
                txn.getAmount(),
                txn.getType(),
                txn.getCreatedAt());
    }

}
