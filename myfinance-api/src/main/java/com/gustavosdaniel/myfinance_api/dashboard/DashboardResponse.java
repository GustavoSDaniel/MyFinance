package com.gustavosdaniel.myfinance_api.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(

        BigDecimal totalIncomes,
        BigDecimal totalExpenses,
        BigDecimal balance,
        List<CategorySum> expensesByCategory

) {
}
