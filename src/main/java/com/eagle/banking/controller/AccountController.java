package com.eagle.banking.controller;

import com.eagle.banking.exception.ForbiddenException;
import com.eagle.banking.model.Account;
import com.eagle.banking.dto.AccountDto;
import com.eagle.banking.service.AccountService;
import com.eagle.banking.service.TransactionService;
import com.eagle.banking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.eagle.banking.helper.AuthHelper.requireAuth;
import static com.eagle.banking.dto.AccountDto.fromEntity;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    public AccountController(AccountService accountService, TransactionService txService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<AccountDto> create(@Valid @RequestBody Account account, Authentication auth) {
        requireAuth(auth);
        // ignore user-supplied userId — use caller
        String callerId = auth.getName();
        account.setUser(userService.getById(callerId));
        Account created = accountService.create(account);
        AccountDto dto = fromEntity(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<AccountDto>> list(Authentication auth) {
        requireAuth(auth);
        String callerId = auth.getName();
        List<AccountDto> dtos = accountService.getByUserId(callerId).stream().map(a -> fromEntity(a)).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> get(@PathVariable String accountId, Authentication auth) {
        requireAuth(auth);
        var account = accountService.getById(accountId);
        if (!auth.getName().equals(account.getUser().getId())) throw new ForbiddenException("forbidden");
        return ResponseEntity.ok(fromEntity(account));
    }

    @PatchMapping("/{accountId}")
    public ResponseEntity<AccountDto> patch(@PathVariable String accountId, @RequestBody Account update, Authentication auth) {
        requireAuth(auth);
        var account = accountService.getById(accountId);
        if (!auth.getName().equals(account.getUser().getId())) throw new ForbiddenException("forbidden");
        if (update.getAccountType() != null) account.setAccountType(update.getAccountType());
        accountService.update(account);
        return ResponseEntity.ok(fromEntity(account));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> delete(@PathVariable String accountId, Authentication auth) {
        requireAuth(auth);
        var account = accountService.getById(accountId);
        if (!auth.getName().equals(account.getUser().getId())) throw new ForbiddenException("forbidden");
        accountService.delete(accountId);
        return ResponseEntity.noContent().build();
    }
}
