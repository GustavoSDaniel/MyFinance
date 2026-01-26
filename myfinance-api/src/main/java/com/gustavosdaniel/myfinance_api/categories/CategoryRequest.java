package com.gustavosdaniel.myfinance_api.categories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CategoryRequest(

        @NotBlank(message = "O nome da categoria é obrigatório")
        String name,
        @NotNull(message = "O tipo da categoria é obrigatório")
        CategoryType type,

        @NotBlank(message = "A cor da categoria é obrigatório")
        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
                message = "Cor deve estar no formato hexadecimal (#FFFFFF)")
        String color
) {
}
