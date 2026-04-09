package com.gustavosdaniel.myfinance_api.domain.dto.response;


import com.gustavosdaniel.myfinance_api.domain.enuns.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponseInfo(

        UUID id,
        String user,
        String name,
        AccountType type,
        String description,
        BigDecimal currentBalance,
        Boolean active
        ) {
}
