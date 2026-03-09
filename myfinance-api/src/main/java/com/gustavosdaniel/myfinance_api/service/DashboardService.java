package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.domain.dto.BetweenDateDashboard;
import com.gustavosdaniel.myfinance_api.domain.dto.DashboardCategorySum;
import com.gustavosdaniel.myfinance_api.domain.dto.DashboardResponse;
import com.gustavosdaniel.myfinance_api.repository.TransactionRepository;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;


@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AuthHelper authHelper;
    private final Logger log = LoggerFactory.getLogger(DashboardService.class);

    public DashboardService(TransactionRepository transactionRepository, AuthHelper authHelper) {
        this.transactionRepository = transactionRepository;
        this.authHelper = authHelper;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<DashboardResponse> getDashboard(OAuth2User principal,
                                                          BetweenDateDashboard betweenDate) {

        User user = authHelper.getCurrentUser(principal);

        log.info("Gerando Dashboard para usuário {} entre {} á {}",
                user.getName(), betweenDate.startDate(), betweenDate.endDate());

        if (betweenDate.endDate().isBefore(betweenDate.startDate()) ){

            throw  new IllegalArgumentException("A data final não pode ser antes da data inicial");
        }

        BigDecimal incomes = transactionRepository.
                sumIncomesByPeriod(user.getId(),
                        betweenDate.startDate().atStartOfDay(),
                        betweenDate.endDate().atTime(LocalTime.MAX));

        BigDecimal expenses = transactionRepository
                .sumExpensesByPeriod(user.getId(),
                        betweenDate.startDate().atStartOfDay(),
                        betweenDate.endDate().atTime(LocalTime.MAX));

        BigDecimal balance = incomes.subtract(expenses);

        List<DashboardCategorySum> expensesByCategory = transactionRepository
                .sumExpensesByCategoryAndPeriod(user.getId(),
                        betweenDate.startDate().atStartOfDay(),
                        betweenDate.endDate().atTime(LocalTime.MAX));


        return ResponseEntity.ok( new DashboardResponse(
                incomes,
                expenses,
                balance,
                expensesByCategory
        ));

    }
}
