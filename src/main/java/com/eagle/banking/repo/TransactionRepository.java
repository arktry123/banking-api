package com.eagle.banking.repo;

import com.eagle.banking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByAccountId(String accountId);
    Optional<Transaction> findByAccountIdAndId(String accountId, String transactionId);

}
