package com.gustavosdaniel.myfinance_api.domain.dto;

import com.gustavosdaniel.myfinance_api.domain.enuns.RecurrenceType;
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
