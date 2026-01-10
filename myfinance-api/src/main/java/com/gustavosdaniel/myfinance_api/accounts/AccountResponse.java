package com.gustavosdaniel.myfinance_api.accounts;


import java.math.BigDecimal;

public record AccountResponse(

        String user,
        String name,
        AccountType type,
        String description,
        BigDecimal initialBalance
        ) {
}
