package com.eagle.banking.service;

import com.eagle.banking.model.Account;

import java.util.List;

public interface AccountService {
    Account create(Account account);

    Account getById(String id);

    List<Account> getByUserId(String userId);

    void delete(String id);

    boolean userHasAccounts(String userId);

    void update(Account account);
}
