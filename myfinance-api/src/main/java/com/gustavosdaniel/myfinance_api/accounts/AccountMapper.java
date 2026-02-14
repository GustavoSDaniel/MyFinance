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
                request.description(),
                request.initialBalance()
                );
    }

    public AccountResponse toAccountResponse(Account account){

        if (account == null){
            return null;
        }

        return new AccountResponse(
                account.getId(),
                account.getUser() != null ? account.getUser().getName() : null,
                account.getName(),
                account.getType(),
                account.getDescription(),
                account.getInitialBalance()
        );
    }

    public AccountResponseInfo toAccountResponseInfo(Account account){

        if (account == null){
            return null;
        }

        return new AccountResponseInfo(
                account.getUser() != null ? account.getUser().getName() : null,
                account.getName(),
                account.getType(),
                account.getDescription(),
                account.getCurrentBalance()
        );
    }

    public void updateAccountFromRequest(AccountUpdateRequest request, Account account){

        if (request.name() != null && !request.name().isBlank()){
            account.setName(request.name());
        }

        if (request.description() != null){

            account.setDescription(request.description());

        }

        if (request.type() != null && !request.type().equals(account.getType())){
            account.setType(request.type());
        }
    }

}
