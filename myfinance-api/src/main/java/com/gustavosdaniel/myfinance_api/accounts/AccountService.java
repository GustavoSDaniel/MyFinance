package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.transactions.TransactionType;
import com.gustavosdaniel.myfinance_api.util.InsufficientBalanceException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountResponse createAccount(AccountRequest accountRequest, UUID userId) throws AccountNameDuplicate;

    List<AccountResponse> getAllAccounts(UUID userId);

    List<AccountResponse> getAllAccountsActive(UUID userId);

    List<AccountResponse> getAllAccountsDisabled(UUID userId);

    AccountResponse getById(UUID id, UUID userId);

    List<AccountResponse> searchAccount(String name, UUID userId);

    void updateBalance(UUID id, UUID userId, BigDecimal value, TransactionType type) throws InvalidAmountException, InsufficientBalanceException;

    AccountResponse updateAccount(UUID id, UUID userId, AccountUpdateRequest request) throws AccountNameDuplicate;

    void activateAccount(UUID id, UUID userId);

    void deactivateAccount(UUID id, UUID userId);

    void deleteAccount(UUID id, UUID userId);
}
