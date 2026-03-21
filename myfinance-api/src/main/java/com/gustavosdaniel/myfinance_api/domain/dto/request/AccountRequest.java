package com.gustavosdaniel.myfinance_api.domain.dto.request;

import com.gustavosdaniel.myfinance_api.domain.enuns.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequest(

        @NotBlank(message = "O nome não deve estar vazio")
        String name,
        @NotNull(message = "O tipo da conta é obrigatório")
        AccountType type,
        BigDecimal initialBalance,
        String description
         ) {
}
