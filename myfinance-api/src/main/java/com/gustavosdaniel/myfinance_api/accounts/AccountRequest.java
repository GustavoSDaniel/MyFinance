package com.gustavosdaniel.myfinance_api.accounts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountRequest(

        @NotBlank(message = "O nome não deve estar vazio")
        String name,
        @NotNull(message = "O tipo da conta é obrigatório")
        AccountType type,
        String description
) {
}
