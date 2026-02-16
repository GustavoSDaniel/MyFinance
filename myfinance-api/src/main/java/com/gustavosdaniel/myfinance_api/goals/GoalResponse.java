package com.gustavosdaniel.myfinance_api.goals;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalResponse(

        String category,
        String name,
        String description,
        BigDecimal currentAmount,
        BigDecimal targetAmount,
        PriorityStatus priority,
        LocalDate deadline) {
}
