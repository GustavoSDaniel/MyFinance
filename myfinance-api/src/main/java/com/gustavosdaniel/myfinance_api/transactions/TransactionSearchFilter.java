package com.gustavosdaniel.myfinance_api.transactions;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionSearchFilter(

        UUID accountId,
        UUID categoryId,
        String description,
        TransactionType type,
        TransactionStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
