package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.accounts.Account;
import com.gustavosdaniel.myfinance_api.accounts.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.categories.CategoryMapper;
import com.gustavosdaniel.myfinance_api.user.User;
import org.springframework.stereotype.Component;


@Component
public class TransactionMapper {

    private final CategoryMapper categoryMapper;

    public TransactionMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public Transaction toTransaction(TransactionRequest request, User user, Account account, Category category) throws InvalidAmountException {

        if (request == null){
            return null;
        }

        return new  Transaction(
                user,
                account,
                category,
                request.description(),
                request.amount(),
                request.type(),
                request.date().atStartOfDay(),
                request.isRecurring(),
                request.recurrenceType()
        );
    }

    public TransactionResponse toTransactionResponse(Transaction transaction){

        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTime(),
                transaction.getStatus(),
                transaction.getAccount().getId(),
                transaction.getAccount().getName(),
                categoryMapper.toCategoryResponse(transaction.getCategory()),
                transaction.getIsRecurring(),
                transaction.getRecurrenceType()
        );
    }
}
