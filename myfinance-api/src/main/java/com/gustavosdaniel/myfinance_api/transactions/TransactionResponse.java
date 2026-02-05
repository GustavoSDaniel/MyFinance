package com.gustavosdaniel.myfinance_api.transactions;



import com.gustavosdaniel.myfinance_api.categories.CategoryResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(

        UUID id,
        String description,
        BigDecimal amount,
        TransactionType type,
        LocalDateTime dateTime,
        TransactionStatus status,
        UUID accountId,
        String accountName,
        CategoryResponse category,
        Boolean isRecurring,
        RecurrenceType recurrenceType
) {
}
