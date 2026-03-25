package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.domain.dto.request.BetweenDate;
import com.gustavosdaniel.myfinance_api.domain.dto.request.CategorySum;
import com.gustavosdaniel.myfinance_api.domain.dto.response.DashboardResponse;
import com.gustavosdaniel.myfinance_api.repository.TransactionRepository;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * Serviço responsável por gerar os dados consolidados do painel de controle (Dashboard).
 * Realiza as agregações financeiras do usuário, como total de receitas, despesas,
 * saldo do período e agrupamento de gastos por categoria.
 */
@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final Logger log = LoggerFactory.getLogger(DashboardService.class);

    public DashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Gera o resumo financeiro (Dashboard) para um usuário dentro de um período específico.
     * Calcula o total de receitas, despesas, saldo e despesas agrupadas por categoria.
     * <p>
     * O período considerado é inclusivo para as datas de início e fim, sendo que a data final
     * é tratada como o final do dia (23:59:59.999...), garantindo que todas as transações
     * do dia final sejam incluídas.
     * </p>
     * <p>
     * Caso não existam transações em uma das categorias de receita ou despesa, o valor
     * será tratado como zero para fins de cálculo do saldo.
     * </p>
     *
     * @param user        Entidade do usuário para o qual o dashboard será gerado.
     * @param betweenDate Objeto (Record/DTO) contendo as datas inicial e final do período a ser analisado.
     * @return DTO contendo os totais consolidados (receitas, despesas, saldo) e a lista de despesas por categoria.
     * @throws IllegalArgumentException Caso a data final informada seja anterior à data inicial.
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User user, BetweenDate betweenDate) {

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

    /**
     * Valida se o período informado é coerente, verificando se a data final é posterior
     * ou igual à data inicial.
     *
     * @param betweenDate Objeto contendo as datas de início e fim do período.
     * @throws IllegalArgumentException Se a data final for anterior à data inicial.
     */
    private void assertValidDateRange(BetweenDate betweenDate){

        if (betweenDate.endDate().isBefore(betweenDate.startDate()) )

            throw  new IllegalArgumentException("A data final não pode ser antes da data inicial");

    }
}
