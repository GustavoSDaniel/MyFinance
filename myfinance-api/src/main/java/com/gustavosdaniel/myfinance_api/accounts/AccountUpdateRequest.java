package com.gustavosdaniel.myfinance_api.accounts;

public record AccountUpdateRequest(

        String name,
        String description,
        AccountType type

) {
}
