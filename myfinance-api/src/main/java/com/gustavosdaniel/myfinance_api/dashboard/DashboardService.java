package com.gustavosdaniel.myfinance_api.dashboard;

import com.gustavosdaniel.myfinance_api.transactions.TransactionRepository;
import com.gustavosdaniel.myfinance_api.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;


@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final Logger log = LoggerFactory.getLogger(DashboardService.class);

    public DashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User user, BetweenDate betweenDate) {

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

        List<CategorySum> expensesByCategory = transactionRepository
                .sumExpensesByCategoryAndPeriod(user.getId(),
                        betweenDate.startDate().atStartOfDay(),
                        betweenDate.endDate().atTime(LocalTime.MAX));


        return new DashboardResponse(
                incomes,
                expenses,
                balance,
                expensesByCategory
        );

    }
}
