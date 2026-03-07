package com.gustavosdaniel.myfinance_api.domain.dto;


import com.gustavosdaniel.myfinance_api.domain.po.AccountType;

import java.math.BigDecimal;

public record AccountResponseInfo(

        String user,
        String name,
        AccountType type,
        String description,
        BigDecimal currentBalance
        ) {
}
