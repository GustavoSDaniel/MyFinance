package com.gustavosdaniel.myfinance_api.domain.dto.request;

import com.gustavosdaniel.myfinance_api.domain.enuns.RecurrenceType;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoalTransferRequest(

        @NotNull(message = "Chave de idempotência é obrigatória")
        UUID idempotencyKey,

        @NotNull(message = "O id da account é obrigatório")
        UUID accountId,

        @NotNull(message = "O id da categoria é obrigatório")
        UUID categoryId,

        @Positive(message = "O valor não pode ser negativo")
        @NotNull(message = "O valor é obrigatório")
        BigDecimal amount,

        @NotNull(message = "Data da transação é obrigatória")
        LocalDate date,

        Boolean isRecurring,

        RecurrenceType recurrenceType,

        String description
) {
}

