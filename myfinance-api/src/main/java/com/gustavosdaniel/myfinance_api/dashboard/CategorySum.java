package com.gustavosdaniel.myfinance_api.dashboard;

import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CategorySum(

        String name,
        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
                message = "Cor deve estar no formato hexadecimal (#FFFFFF)")
        String color,
        BigDecimal totalAmount
) {

    public CategorySum{
        if (totalAmount == null){
            totalAmount = BigDecimal.ZERO;
        }
    }
}
