package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.user.User;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountResponse createAccount(AccountRequest accountRequest, User user) throws AccountNameDuplicate;

    List<AccountResponseInfo> getAllAccounts(UUID userId, String status);

    AccountResponseInfo getById(UUID id, UUID userId);

    List<AccountResponseInfo> searchAccount(String name, UUID userId);

    AccountResponseInfo updateAccount(UUID id, UUID userId, AccountUpdateRequest request) throws AccountNameDuplicate;

    void activateAccount(UUID id, UUID userId);

    void deactivateAccount(UUID id, UUID userId);

    void deleteAccount(UUID id, User user);
}
