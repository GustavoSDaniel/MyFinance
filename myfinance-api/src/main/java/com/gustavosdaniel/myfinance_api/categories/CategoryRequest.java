package com.gustavosdaniel.myfinance_api.categories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(

        @NotBlank(message = "O nome da categoria é obrigatório")
        String name,
        @NotNull(message = "O tipo da categoria é obrigatório")
        CategoryType type,
        @NotBlank(message = "A cor da categoria é obrigatório")
        String color
) {
}
