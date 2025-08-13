package com.eagle.banking.controller;

import com.eagle.banking.exception.ConflictException;
import com.eagle.banking.exception.ForbiddenException;
import com.eagle.banking.model.User;
import com.eagle.banking.service.AccountService;
import com.eagle.banking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;
    private final AccountService accountService;

    public UserController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    // create user - public
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // fetch user (only own)
    @GetMapping("/{userId}")
    public ResponseEntity<User> get(@PathVariable String userId, Authentication auth) {
        requireAuth(auth);
        User user = userService.getById(userId);
        String callerId = auth.getName();
        if (!callerId.equals(userId)) throw new ForbiddenException("cannot fetch other user's details");
        return ResponseEntity.ok(user);
    }

    // patch/update (only own)
    @PatchMapping("/{userId}")
    public ResponseEntity<User> patch(@PathVariable String userId, @RequestBody User update, Authentication auth) {
        requireAuth(auth);
        User user = userService.getById(userId);
        String callerId = auth.getName();
        if (!callerId.equals(user.getId())) throw new ForbiddenException("cannot update other user's details");
        return ResponseEntity.ok(userService.update(userId, update));
    }

    // delete (only own and if no accounts)
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable String userId, Authentication auth) {
        requireAuth(auth);
        User user = userService.getById(userId);
        String callerId = auth.getName();
        if (!callerId.equals(user.getId())) throw new ForbiddenException("cannot delete other user's details");
        if (accountService.userHasAccounts(userId)) {
            throw new ConflictException("user has accounts");
        }
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    private void requireAuth(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new ForbiddenException("authentication required");
    }
}
