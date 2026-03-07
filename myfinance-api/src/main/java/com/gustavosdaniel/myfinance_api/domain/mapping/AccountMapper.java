package com.gustavosdaniel.myfinance_api.domain.mapping;

import com.gustavosdaniel.myfinance_api.domain.dto.AccountRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountUpdateRequest;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.user.User;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toAccount(User user, AccountRequest request){

        if (request == null){
            return null;
        }

        return new Account(
                user,
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
