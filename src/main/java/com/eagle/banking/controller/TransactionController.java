package com.eagle.banking.controller;

import com.eagle.banking.dto.TransactionDto;
import com.eagle.banking.exception.ForbiddenException;
import com.eagle.banking.exception.InsufficientFundsException;
import com.eagle.banking.exception.InvalidRequestException;
import com.eagle.banking.model.Account;
import com.eagle.banking.model.Transaction;
import com.eagle.banking.model.TransactionType;
import com.eagle.banking.service.AccountService;
import com.eagle.banking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static com.eagle.banking.dto.TransactionDto.fromEntity;
import static com.eagle.banking.helper.AuthHelper.requireAuth;

@RestController
@RequestMapping("/v1/accounts/{accountId}/transactions")
public class TransactionController {

    private final TransactionService txService;
    private final AccountService accountService;

    public TransactionController(TransactionService txService, AccountService accountService) {
        this.txService = txService;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<TransactionDto> create(@PathVariable String accountId,
                                                 @Valid @RequestBody Transaction req,
                                                 Authentication auth) {
        requireAuth(auth);
        // validate account exists and owner
        Account account = accountService.getById(accountId);
        if (!auth.getName().equals(account.getUser().getId())) throw new ForbiddenException("forbidden");
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("amount must be greater than 0");
        }

        TransactionType type = req.getType();
        if (TransactionType.WITHDRAW.equals(type)) {
            if (account.getBalance().compareTo(req.getAmount()) < 0) {
                throw new InsufficientFundsException("insufficient funds");
            }
            account.setBalance(account.getBalance().subtract(req.getAmount()));
        } else if (TransactionType.DEPOSIT.equals(type)) {
            account.setBalance(account.getBalance().add(req.getAmount()));
        } else {
            throw new InvalidRequestException("type must be deposit or withdrawal");
        }

        // store updated account (in-memory)
        // accountService already stores the object by reference

        req.setAccount(account);
        Transaction recorded = txService.record(req);

        return ResponseEntity.status(HttpStatus.CREATED).body(fromEntity(recorded));
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> list(@PathVariable String accountId, Authentication auth) {
        requireAuth(auth);
        var account = accountService.getById(accountId);
        if (!auth.getName().equals(account.getUser().getId())) throw new ForbiddenException("forbidden");
        List<TransactionDto> transactions = txService.listForAccount(accountId).stream()
                .map(t -> fromEntity(t))
                .toList();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> get(@PathVariable String accountId,
                                              @PathVariable String transactionId,
                                              Authentication auth) {
        requireAuth(auth);
        var account = accountService.getById(accountId);
        if (!auth.getName().equals(account.getUser().getId())) throw new ForbiddenException("forbidden");
        var txOpt = txService.find(accountId, transactionId);
        if (txOpt.isEmpty()) throw new InvalidRequestException("transaction not found");
        return ResponseEntity.ok(fromEntity(txOpt.get()));
    }

}
