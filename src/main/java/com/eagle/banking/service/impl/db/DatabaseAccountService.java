package com.eagle.banking.service.impl.db;

import com.eagle.banking.exception.ResourceNotFoundException;
import com.eagle.banking.model.Account;
import com.eagle.banking.repo.AccountRepository;
import com.eagle.banking.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DatabaseAccountService implements AccountService {

    private final AccountRepository accountRepository;

    public DatabaseAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account create(Account account) {
        account.setAccountNumber("ACCT-" + System.currentTimeMillis());
        return accountRepository.save(account);
    }

    @Override
    public Account getById(String id) {
        Optional<Account> account = accountRepository.findById(id);
        if (account.isEmpty()) throw new ResourceNotFoundException("Account not found: " + id);
        return account.get();
    }

    @Override
    public List<Account> getByUserId(String userId) {
        return accountRepository.findByUserId(userId);
    }

    @Override
    public void delete(String id) {
        Optional<Account> existingAccount = accountRepository.findById(id);
        if (existingAccount.isPresent()) {
            accountRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Account not found: " + id);
        }
    }

    @Override
    public boolean userHasAccounts(String userId) {
        return !getByUserId(userId).isEmpty();
    }

    @Override
    public void update(Account account) {
        accountRepository.save(account);
    }
}
