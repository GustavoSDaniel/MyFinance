package com.gustavosdaniel.myfinance_api.accounts;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountResponse createAccount(AccountRequest accountRequest, UUID userId) throws AccountNameDuplicate;

    List<AccountResponse> getAllAccounts(UUID userId);

    List<AccountResponse> getAllAccountsActive(UUID userId);

    List<AccountResponse> getAllAccountsDisabled(UUID userId);

    AccountResponse getById(UUID id, UUID userId);

    List<AccountResponse> searchAccount(String name, UUID userId);

    void deleteAccount(UUID id, UUID userId);
}
