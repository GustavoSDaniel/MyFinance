package com.gustavosdaniel.myfinance_api.domain.dto.request;

import com.gustavosdaniel.myfinance_api.domain.enuns.RecurrenceType;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(

        String description,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal amount,

        @NotNull(message = "O tipo de transação é obrigatório")
        TransactionType type,

        @NotNull(message = "A conta é obrigatória")
        UUID accountId,

        @NotNull(message = "A categoria é obrigatória")
        UUID categoryId,

        @NotNull(message = "Data da transação é obrigatória")
        LocalDate date,

        Boolean isRecurring,

        RecurrenceType recurrenceType,

        @NotNull(message = "Chave de idempotência é obrigatória")
        UUID idempotencyKey) {
}
