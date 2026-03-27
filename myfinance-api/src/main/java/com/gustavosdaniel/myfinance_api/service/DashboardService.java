package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.domain.dto.BetweenDateDashboard;
import com.gustavosdaniel.myfinance_api.domain.dto.DashboardCategorySum;
import com.gustavosdaniel.myfinance_api.domain.dto.DashboardResponse;
import com.gustavosdaniel.myfinance_api.repository.TransactionRepository;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;


/**
 * Serviço responsável por gerar os dados do dashboard financeiro do usuário.
 *
 * <p>O dashboard apresenta um resumo das movimentações financeiras dentro
 * de um determinado período, incluindo:
 * <ul>
 *     <li>Total de receitas</li>
 *     <li>Total de despesas</li>
 *     <li>Saldo final</li>
 *     <li>Despesas agrupadas por categoria</li>
 * </ul>
 *
 * <p>Todos os dados são filtrados com base no usuário autenticado.
 */
@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AuthHelper authHelper;
    private final Logger log = LoggerFactory.getLogger(DashboardService.class);

    public DashboardService(TransactionRepository transactionRepository, AuthHelper authHelper) {
        this.transactionRepository = transactionRepository;
        this.authHelper = authHelper;
    }

    /**
     * Gera o resumo financeiro do dashboard para o usuário autenticado.
     *
     * <p>O resumo inclui:
     * <ul>
     *     <li>Total de receitas no período</li>
     *     <li>Total de despesas no período</li>
     *     <li>Saldo final (receitas - despesas)</li>
     *     <li>Lista de despesas agrupadas por categoria</li>
     * </ul>
     *
     * <p>As consultas são filtradas pelo intervalo de datas informado.
     *
     * @param jwt usuário autenticado
     * @param betweenDate intervalo de datas utilizado para filtrar as transações
     * @return objeto contendo os dados do dashboard financeiro
     * @throws IllegalArgumentException caso a data final seja anterior à data inicial
     */
    @Transactional(readOnly = true)
    public ResponseEntity<DashboardResponse> getDashboard(Jwt jwt,
                                                          BetweenDateDashboard betweenDate) {

        User user = authHelper.getCurrentUser(jwt);

        log.info("Gerando Dashboard para usuário {} entre {} á {}",
                user.getName(), betweenDate.startDate(), betweenDate.endDate());

        assertValidDateRange(betweenDate);

        BigDecimal incomes = transactionRepository.
                sumIncomesByPeriod(user.getId(),
                        betweenDate.startDate().atStartOfDay(),
                        betweenDate.endDate().atTime(LocalTime.MAX));

        BigDecimal expenses = transactionRepository
                .sumExpensesByPeriod(user.getId(),
                        betweenDate.startDate().atStartOfDay(),
                        betweenDate.endDate().atTime(LocalTime.MAX));

        BigDecimal incomesSafe = incomes != null ? incomes : BigDecimal.ZERO;
        BigDecimal expensesSafe = expenses != null ? expenses : BigDecimal.ZERO;

        BigDecimal balance = incomesSafe.subtract(expensesSafe);

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

    /**
     * Valida se o período informado é coerente, verificando se a data final é posterior
     * ou igual à data inicial.
     *
     * @param betweenDate Objeto contendo as datas de início e fim do período.
     * @throws IllegalArgumentException Se a data final for anterior à data inicial.
     */
    private void assertValidDateRange(BetweenDateDashboard betweenDate){

        if (betweenDate.endDate().isBefore(betweenDate.startDate()) )

            throw  new IllegalArgumentException("A data final não pode ser antes da data inicial");

    }
}
