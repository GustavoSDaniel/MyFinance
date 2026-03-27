package com.gustavosdaniel.myfinance_api.domain.dto.response;

import com.gustavosdaniel.myfinance_api.domain.dto.request.DashboardCategorySum;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(

        BigDecimal totalIncomes,
        BigDecimal totalExpenses,
        BigDecimal balance,
        List<DashboardCategorySum> expensesByCategory

) {
}
