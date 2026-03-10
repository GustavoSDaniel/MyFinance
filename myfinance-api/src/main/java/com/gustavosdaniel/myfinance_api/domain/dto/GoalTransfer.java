package com.gustavosdaniel.myfinance_api.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record GoalTransfer(

        @NotNull(message = "Chave de idempotência é obrigatória")
        UUID idempotencyKey,

        @NotNull(message = "O id da account é obrigatório")
        UUID accountId,

        @Positive(message = "O valor não pode ser negativo")
        @NotNull(message = "O valor é obrigatório")
        BigDecimal amount,

        String description
) {
}

