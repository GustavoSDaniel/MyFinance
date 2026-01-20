package com.gustavosdaniel.myfinance_api.accounts;


import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(

        UUID id,
        String user,
        String name,
        AccountType type,
        String description,
        BigDecimal initialBalance
        ) {
}
