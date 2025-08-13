package com.eagle.banking.service.impl.db;

import com.eagle.banking.exception.ResourceNotFoundException;
import com.eagle.banking.model.Account;
import com.eagle.banking.model.User;
import com.eagle.banking.repo.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseAccountServiceTest {

    private AccountRepository accountRepository;
    private DatabaseAccountService accountService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        accountService = new DatabaseAccountService(accountRepository);
    }

    @Test
    void create_ShouldGenerateAccountNumberAndSave() {
        Account account = new Account();
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.create(account);

        assertNotNull(result);
        assertTrue(account.getAccountNumber().startsWith("ACCT-"));
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void getById_ShouldReturnAccount_WhenExists() {
        Account account = new Account();
        account.setId("1");
        when(accountRepository.findById("1")).thenReturn(Optional.of(account));

        Account result = accountService.getById("1");

        assertEquals("1", result.getId());
        verify(accountRepository).findById("1");
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        when(accountRepository.findById("2")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> accountService.getById("2"));
        assertEquals("Account not found: 2", ex.getMessage());
    }

    @Test
    void getByUserId_ShouldReturnList() {
        Account account = new Account();
        User user = new User();
        user.setId("user1");
        account.setUser(user);
        when(accountRepository.findByUserId("user1")).thenReturn(List.of(account));

        List<Account> result = accountService.getByUserId("user1");

        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUser().getId());
        verify(accountRepository).findByUserId("user1");
    }

    @Test
    void delete_ShouldDelete_WhenExists() {
        Account account = new Account();
        account.setId("1");
        when(accountRepository.findById("1")).thenReturn(Optional.of(account));

        accountService.delete("1");

        verify(accountRepository).deleteById("1");
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(accountRepository.findById("1")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> accountService.delete("1"));
        assertEquals("Account not found: 1", ex.getMessage());
    }

    @Test
    void userHasAccounts_ShouldReturnTrue_WhenAccountsExist() {
        when(accountRepository.findByUserId("u1")).thenReturn(List.of(new Account()));

        boolean result = accountService.userHasAccounts("u1");

        assertTrue(result);
    }

    @Test
    void userHasAccounts_ShouldReturnFalse_WhenNoAccounts() {
        when(accountRepository.findByUserId("u1")).thenReturn(Collections.emptyList());

        boolean result = accountService.userHasAccounts("u1");

        assertFalse(result);
    }

    @Test
    void update_ShouldCallSave() {
        Account account = new Account();
        account.setId("123");

        accountService.update(account);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals("123", captor.getValue().getId());
    }
}
