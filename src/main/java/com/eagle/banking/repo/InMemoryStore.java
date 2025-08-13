package com.eagle.banking.repo;

import com.eagle.banking.model.Account;
import com.eagle.banking.model.Transaction;
import com.eagle.banking.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryStore {
    public final Map<String, User> users = new ConcurrentHashMap<>();
    public final Map<String, Account> accounts = new ConcurrentHashMap<>();
    public final Map<String, List<Transaction>> transactions = new ConcurrentHashMap<>();
}