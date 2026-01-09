package com.gustavosdaniel.myfinance_api.accounts;

import java.util.UUID;

public interface AccountService {

    AccountResponse createAccount(AccountRequest accountRequest, UUID userId) throws AccountNameDuplicate;
}
