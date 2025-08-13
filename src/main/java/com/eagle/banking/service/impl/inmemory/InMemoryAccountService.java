package com.eagle.banking.service.impl.inmemory;

import com.eagle.banking.exception.ResourceNotFoundException;
import com.eagle.banking.model.Account;
import com.eagle.banking.service.AccountService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("local")
public class InMemoryAccountService implements AccountService {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public Account create(Account account) {
        account.setAccountNumber("ACCT-" + System.currentTimeMillis());
        accounts.put(account.getId(), account);
        return account;
    }

    @Override
    public Account getById(String id) {
        Account a = accounts.get(id);
        if (a == null) throw new ResourceNotFoundException("Account not found: " + id);
        return a;
    }

    @Override
    public List<Account> getByUserId(String userId) {
        List<Account> list = new ArrayList<>();
        accounts.values().stream()
                .filter(a -> userId.equals(a.getUser().getId()))
                .forEach(list::add);
        return list;
    }

    public Account deposit(String accountId, BigDecimal amount) {
        Account a = getById(accountId);
        a.setBalance(a.getBalance().add(amount));
        return a;
    }

    public Account withdraw(String accountId, BigDecimal amount) {
        Account a = getById(accountId);
        if (a.getBalance().compareTo(amount) < 0) {
            // handled at controller/service caller via InsufficientFundsException
            throw new RuntimeException("insufficient funds"); // controller will catch & turn into InsufficientFundsException
        }
        a.setBalance(a.getBalance().subtract(amount));
        return a;
    }

    @Override
    public void delete(String id) {
        if (accounts.remove(id) == null) throw new ResourceNotFoundException("Account not found: " + id);
    }

    @Override
    public boolean userHasAccounts(String userId) {
        return accounts.values().stream().anyMatch(a -> userId.equals(a.getUser().getId()));
    }

    @Override
    public void update(Account account) {
        accounts.values().stream()
                .filter(a -> a.getId().equals(account.getId()))
                .findFirst().map(a -> {
                    a.setAccountType(account.getAccountType());
                    return a;
                }).orElseThrow(() -> new ResourceNotFoundException("Account not found: " + account.getId()));
    }
}
