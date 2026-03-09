package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.exception.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.exception.InsufficientBalanceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(User user, TransactionRequest request) throws InvalidAmountException, InsufficientBalanceException;

    void transactionConfirmed(UUID id, UUID userId) throws InvalidAmountException, InsufficientBalanceException;

    void transactionCancel(UUID id, UUID userId) throws InvalidAmountException, InsufficientBalanceException;

    void transfer(User user, TransferRequest transferRequest) throws InvalidAmountException, InsufficientBalanceException;

    TransactionResponse getTransactionById(UUID id, UUID userId);

    Page<TransactionResponse> getAllWithFilter(User user, TransactionSearchFilter filter, Pageable pageable);

    void deleteTransaction(UUID id, UUID userId);
}
