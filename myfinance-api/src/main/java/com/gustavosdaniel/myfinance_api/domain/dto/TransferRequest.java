package com.gustavosdaniel.myfinance_api.domain.dto;

import com.gustavosdaniel.myfinance_api.domain.enuns.RecurrenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransferRequest(

        @NotNull(message = "O id da conta de que está sendo enviada é obrigatória")
        UUID fromAccountId,

        @NotNull(message = "O id da conta que está sendo recebida é obrigatório")
        UUID toAccountId,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal amount,

        @NotNull(message = "A categoria é obrigatória")
        UUID categoryId,

        @NotNull(message = "Chave de idempotência é obrigatória")
        UUID idempotencyKey,

        String description,

        LocalDate date,

        Boolean isRecurring,

        RecurrenceType recurrenceType
) {
}






