package com.gustavosdaniel.myfinance_api.categories;

import jakarta.validation.constraints.Pattern;

public record CategoryRequestUpdate(

        String name,
        CategoryType type,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
                message = "Cor deve estar no formato hexadecimal (#FFFFFF)")
        String color,

        String description
) {
}
