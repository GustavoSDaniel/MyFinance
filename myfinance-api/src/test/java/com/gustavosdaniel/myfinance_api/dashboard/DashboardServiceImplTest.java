package com.gustavosdaniel.myfinance_api.dashboard;

import com.gustavosdaniel.myfinance_api.transactions.TransactionRepository;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Nested
    class getDashboard{

        @Test
        @DisplayName("Get deashboard with sucesso")
        void getDashboardWithSucesso(){

            UUID userId = UUID.randomUUID();

            BigDecimal incomers = new BigDecimal("1356.86");
            BigDecimal expenses = new BigDecimal("932.78");
            BigDecimal balance = incomers.subtract(expenses);

            LocalDate start = LocalDate.of(2026, 1, 16);
            LocalDate end = LocalDate.of(2026, 12, 23);
            BetweenDate betweenDate = new BetweenDate(start, end);

            LocalDateTime expectedStart = start.atStartOfDay();
            LocalDateTime expectedEnd = end.atTime(LocalTime.MAX);


            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            CategorySum categorySum = new CategorySum("Comida", "#FFFFFF", new BigDecimal("600"));
            CategorySum categorySum2 = new CategorySum("Transporte", "#FFFFFF", new BigDecimal("800"));
            CategorySum categorySum3 = new CategorySum("Escola", "#FFFFFF", new BigDecimal("350"));

            List<CategorySum> categorySums = Arrays.asList(categorySum, categorySum2, categorySum3);

            DashboardResponse response = new DashboardResponse(incomers, expenses, balance, categorySums);

            when(transactionRepository.sumIncomesByPeriod(userId, expectedStart, expectedEnd)).thenReturn(incomers);
            when(transactionRepository.sumExpensesByPeriod(userId, expectedStart, expectedEnd)).thenReturn(expenses);
            when(transactionRepository
                    .sumExpensesByCategoryAndPeriod(userId, expectedStart, expectedEnd)).thenReturn(categorySums);

            DashboardResponse output = dashboardService.getDashboard(user, betweenDate);

            assertNotNull(output);
            assertEquals(response, output);

            verify(transactionRepository).sumIncomesByPeriod(userId, expectedStart, expectedEnd);
            verify(transactionRepository).sumExpensesByPeriod(userId, expectedStart, expectedEnd);
            verify(transactionRepository).sumExpensesByCategoryAndPeriod(userId, expectedStart, expectedEnd);

        }
    }

}