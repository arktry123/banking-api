package com.eagle.banking.dto;

import com.eagle.banking.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {
    private String id = UUID.randomUUID().toString();

    private String userId;

    private String accountNumber;

    private String accountType;

    private BigDecimal balance = BigDecimal.ZERO;

    public static AccountDto fromEntity(Account account) {
        return new AccountDto(
                account.getId(),
                account.getUser().getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance()
        );
    }

}
