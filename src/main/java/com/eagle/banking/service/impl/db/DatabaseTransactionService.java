package com.eagle.banking.service.impl.db;

import com.eagle.banking.model.Transaction;
import com.eagle.banking.repo.TransactionRepository;
import com.eagle.banking.service.TransactionService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Profile("!local")
public class DatabaseTransactionService implements TransactionService {

    private final TransactionRepository transactionRepository;

    public DatabaseTransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction record(Transaction tx) {
        return transactionRepository.save(tx);
    }

    @Override
    public List<Transaction> listForAccount(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Override
    public Optional<Transaction> find(String accountId, String txId) {
        return transactionRepository.findByAccountIdAndId(accountId,txId);
    }
}
