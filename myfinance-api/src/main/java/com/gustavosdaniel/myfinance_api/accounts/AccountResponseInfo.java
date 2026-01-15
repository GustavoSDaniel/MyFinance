package com.gustavosdaniel.myfinance_api.accounts;


import java.math.BigDecimal;

public record AccountResponseInfo(

        String user,
        String name,
        AccountType type,
        String description,
        BigDecimal currentBalance
        ) {
}
