package com.gustavosdaniel.myfinance_api.domain.dto;



import com.gustavosdaniel.myfinance_api.domain.enuns.RecurrenceType;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionStatus;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;

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
