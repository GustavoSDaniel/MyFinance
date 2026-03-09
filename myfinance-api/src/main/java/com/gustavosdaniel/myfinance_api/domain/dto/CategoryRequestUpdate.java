package com.gustavosdaniel.myfinance_api.domain.dto;

import com.gustavosdaniel.myfinance_api.domain.enuns.CategoryType;
import jakarta.validation.constraints.Pattern;

public record CategoryRequestUpdate(

        String name,
        CategoryType type,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
                message = "Cor deve estar no formato hexadecimal (#FFFFFF)")
        String color,

        String description,

        String icon
) {
}
