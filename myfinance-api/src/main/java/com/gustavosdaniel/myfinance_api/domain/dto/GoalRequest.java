package com.gustavosdaniel.myfinance_api.domain.dto;

import com.gustavosdaniel.myfinance_api.domain.enuns.PriorityStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoalRequest(

        @NotNull
        UUID categoryId,

        @NotBlank(message = "O campo nome é obrigatório")
        String name,

        String description,

        @NotNull(message = "O valor da meta é obrigatório")
        @Positive(message = "O valor não pode ser negativo")
        BigDecimal targetAmount,

        @NotNull(message = "A data final da meta é obrigatório")
        @Future(message = "Data limite deve ser no futuro")
        LocalDate deadLine,

        @NotNull(message = "A prioridade é obrigatória")
        PriorityStatus priority
) {
}
