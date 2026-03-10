package com.gustavosdaniel.myfinance_api.domain.dto;

import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionStatus;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;

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
