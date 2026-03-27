package com.gustavosdaniel.myfinance_api.domain.dto.request;

import com.gustavosdaniel.myfinance_api.domain.enuns.PriorityStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record GoalRequestUpdate(

        UUID categoryId,

        String name,

        String description,

        @Future(message = "Data limite deve ser no futuro")
        LocalDate deadLine,

        @NotNull(message = "A prioridade é obrigatória")
        PriorityStatus priority
) {
}
