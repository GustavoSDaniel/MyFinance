package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.accounts.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.InsufficientBalanceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(TransactionProfile profile, TransactionRequest request) throws InvalidAmountException, InsufficientBalanceException;

    void transactionConfirmed(UUID id, UUID userId) throws InvalidAmountException, InsufficientBalanceException;

    void transactionCancel(UUID id, UUID userId) throws InvalidAmountException, InsufficientBalanceException;

    TransactionResponse getTransactionById(UUID id, UUID userId);

    Page<TransactionResponse> getAllWithFilter(User user, TransactionSearchFilter filter, Pageable pageable);

    void deleteTransaction(UUID id, UUID userId);
}
