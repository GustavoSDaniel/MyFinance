package com.gustavosdaniel.myfinance_api.domain.dto.response;


import com.gustavosdaniel.myfinance_api.domain.enuns.AccountType;

import java.math.BigDecimal;

public record AccountResponseInfo(

        String user,
        String name,
        AccountType type,
        String description,
        BigDecimal currentBalance
        ) {
}
