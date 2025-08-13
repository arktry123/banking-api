package com.eagle.banking.service;

import com.eagle.banking.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Transaction record(Transaction tx);

    List<Transaction> listForAccount(String accountId);

    Optional<Transaction> find(String accountId, String txId);
}
