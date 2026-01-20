package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.transactions.TransactionType;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.InsufficientBalanceException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountResponse createAccount(AccountRequest accountRequest, User user) throws AccountNameDuplicate;

    List<AccountResponseInfo> getAllAccounts(UUID userId);

    List<AccountResponseInfo> getAllAccountsActive(UUID userId);

    List<AccountResponseInfo> getAllAccountsDisabled(UUID userId);

    AccountResponseInfo getById(UUID id, UUID userId);

    List<AccountResponseInfo> searchAccount(String name, UUID userId);

    void updateBalance(UUID id, UUID userId, BigDecimal value, TransactionType type) throws InvalidAmountException, InsufficientBalanceException;

    AccountResponseInfo updateAccount(UUID id, UUID userId, AccountUpdateRequest request) throws AccountNameDuplicate;

    void activateAccount(UUID id, UUID userId);

    void deactivateAccount(UUID id, UUID userId);

    void deleteAccount(UUID id, UUID userId);
}
