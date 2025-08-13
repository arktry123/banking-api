package com.eagle.banking.service.impl.inmemory;

import com.eagle.banking.model.Transaction;
import com.eagle.banking.service.TransactionService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("local")
public class InMemoryTransactionService implements TransactionService {
    // map accountId -> list of transactions
    private final Map<String, List<Transaction>> txStore = new ConcurrentHashMap<>();

    @Override
    public Transaction record(Transaction tx) {
        txStore.computeIfAbsent(tx.getAccount().getId(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(tx);
        return tx;
    }

    @Override
    public List<Transaction> listForAccount(String accountId) {
        return txStore.getOrDefault(accountId, Collections.emptyList());
    }

    @Override
    public Optional<Transaction> find(String accountId, String txId) {
        return listForAccount(accountId).stream().filter(t -> t.getId().equals(txId)).findFirst();
    }
}
