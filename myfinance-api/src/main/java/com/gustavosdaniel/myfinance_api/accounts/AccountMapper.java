package com.gustavosdaniel.myfinance_api.accounts;

import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toAccount(AccountRequest request){

        if (request == null){
            return null;
        }

        return new Account(
                null,
                request.name(),
                request.type(),
                request.description()
        );
    }

    public AccountResponse toAccountResponse(Account account){

        if (account == null){
            return null;
        }

        return new AccountResponse(
                account.getUser() != null ? account.getUser().getName() : null,
                account.getName(),
                account.getType(),
                account.getDescription(),
                account.getInitialBalance()
        );
    }
}
