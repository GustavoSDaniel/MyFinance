package com.gustavosdaniel.myfinance_api.dashboard;

import com.gustavosdaniel.myfinance_api.domain.dto.BetweenDateDashboard;
import com.gustavosdaniel.myfinance_api.domain.dto.DashboardCategorySum;
import com.gustavosdaniel.myfinance_api.domain.dto.DashboardResponse;
import com.gustavosdaniel.myfinance_api.repository.TransactionRepository;
import com.gustavosdaniel.myfinance_api.service.DashboardService;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.domain.enuns.UserRole;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    @Mock
    private OAuth2User principal;

    @Mock
    private AuthHelper authHelper;

    @InjectMocks
    private DashboardService dashboardService;

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
            BetweenDateDashboard betweenDate = new BetweenDateDashboard(start, end);

            LocalDateTime expectedStart = start.atStartOfDay();
            LocalDateTime expectedEnd = end.atTime(LocalTime.MAX);


            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            DashboardCategorySum categorySum = new DashboardCategorySum("Comida", "#FFFFFF", new BigDecimal("600"));
            DashboardCategorySum categorySum2 = new DashboardCategorySum("Transporte", "#FFFFFF", new BigDecimal("800"));
            DashboardCategorySum categorySum3 = new DashboardCategorySum("Escola", "#FFFFFF", new BigDecimal("350"));

            List<DashboardCategorySum> categorySums = Arrays.asList(categorySum, categorySum2, categorySum3);

            DashboardResponse response = new DashboardResponse(incomers, expenses, balance, categorySums);

            when(transactionRepository.sumIncomesByPeriod(userId, expectedStart, expectedEnd)).thenReturn(incomers);
            when(transactionRepository.sumExpensesByPeriod(userId, expectedStart, expectedEnd)).thenReturn(expenses);
            when(transactionRepository
                    .sumExpensesByCategoryAndPeriod(userId, expectedStart, expectedEnd))
                    .thenReturn(categorySums);
            when(authHelper.getCurrentUser(principal)).thenReturn(user);

            ResponseEntity<DashboardResponse> output = dashboardService
                    .getDashboard(principal, betweenDate);

            assertNotNull(output);
            assertEquals(response, output.getBody());

            verify(transactionRepository).sumIncomesByPeriod(userId, expectedStart, expectedEnd);
            verify(transactionRepository).sumExpensesByPeriod(userId, expectedStart, expectedEnd);
            verify(transactionRepository).sumExpensesByCategoryAndPeriod(userId, expectedStart, expectedEnd);

        }
    }

}