package com.gustavosdaniel.myfinance_api.domain.dto;

import com.gustavosdaniel.myfinance_api.domain.enuns.PriorityStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoalResponse(

        UUID id,
        String category,
        String name,
        String description,
        BigDecimal currentAmount,
        BigDecimal targetAmount,
        PriorityStatus priority,
        LocalDate deadline) {
}
