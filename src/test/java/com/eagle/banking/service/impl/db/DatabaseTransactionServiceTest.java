package com.eagle.banking.service.impl.db;

import com.eagle.banking.model.Account;
import com.eagle.banking.model.Transaction;
import com.eagle.banking.repo.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseTransactionServiceTest {

    private TransactionRepository transactionRepository;
    private DatabaseTransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        transactionService = new DatabaseTransactionService(transactionRepository);
    }

    @Test
    void record_ShouldSaveTransaction() {
        Transaction tx = new Transaction();
        when(transactionRepository.save(tx)).thenReturn(tx);

        Transaction result = transactionService.record(tx);

        assertNotNull(result);
        assertSame(tx, result);
        verify(transactionRepository).save(tx);
    }

    @Test
    void listForAccount_ShouldReturnTransactions() {
        String accountId = "acct-1";
        Transaction tx = new Transaction();
        Account account = new Account();
        account.setId(accountId);
        tx.setAccount(account);

        when(transactionRepository.findByAccountId(accountId)).thenReturn(List.of(tx));

        List<Transaction> result = transactionService.listForAccount(accountId);

        assertEquals(1, result.size());
        assertEquals(accountId, result.get(0).getAccount().getId());
        verify(transactionRepository).findByAccountId(accountId);
    }

    @Test
    void find_ShouldReturnTransaction_WhenExists() {
        String accountId = "acct-1";
        String txId = "tx-1";
        Transaction tx = new Transaction();
        tx.setId(txId);
        Account account = new Account();
        account.setId(accountId);
        tx.setAccount(account);

        when(transactionRepository.findByAccountIdAndId(accountId, txId))
                .thenReturn(Optional.of(tx));

        Optional<Transaction> result = transactionService.find(accountId, txId);

        assertTrue(result.isPresent());
        assertEquals(txId, result.get().getId());
        verify(transactionRepository).findByAccountIdAndId(accountId, txId);
    }

    @Test
    void find_ShouldReturnEmpty_WhenNotExists() {
        String accountId = "acct-1";
        String txId = "tx-2";

        when(transactionRepository.findByAccountIdAndId(accountId, txId))
                .thenReturn(Optional.empty());

        Optional<Transaction> result = transactionService.find(accountId, txId);

        assertFalse(result.isPresent());
        verify(transactionRepository).findByAccountIdAndId(accountId, txId);
    }
}
