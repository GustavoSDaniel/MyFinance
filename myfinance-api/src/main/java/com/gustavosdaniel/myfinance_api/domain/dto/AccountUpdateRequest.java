package com.gustavosdaniel.myfinance_api.domain.dto;

import com.gustavosdaniel.myfinance_api.domain.po.AccountType;

public record AccountUpdateRequest(

        String name,
        String description,
        AccountType type

) {
}
