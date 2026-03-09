package com.gustavosdaniel.myfinance_api.domain.dto;

import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record DashboardCategorySum(

        String name,
        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
                message = "Cor deve estar no formato hexadecimal (#FFFFFF)")
        String color,
        BigDecimal totalAmount
) {

    public DashboardCategorySum {
        if (totalAmount == null){
            totalAmount = BigDecimal.ZERO;
        }
    }
}
