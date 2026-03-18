package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.dashboard.CategorySum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório responsável por operações de persistência da entidade {@link Transaction}.
 * <p>
 * Esta interface estende {@link JpaRepository} para fornecer operações CRUD básicas,
 * bem como {@link JpaSpecificationExecutor} para consultas dinâmicas baseadas em Specification.
 * Inclui métodos personalizados para buscar transações por usuário, verificar chave de idempotência
 * e calcular somatórios de receitas e despesas em períodos específicos.
 * </p>
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    /**
     * Busca uma transação pelo seu ID e pelo ID do usuário proprietário.
     *
     * @param id     Identificador único da transação (UUID)
     * @param userId Identificador único do usuário (UUID)
     * @return Um {@link Optional} contendo a transação encontrada, ou vazio se não existir
     */
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Verifica se já existe uma transação com a chave de idempotência fornecida para um determinado usuário.
     * <p>
     * Esse método é utilizado para evitar duplicidade de transações em operações de criação.
     * </p>
     *
     * @param idempotencyKey Chave de idempotência (UUID) que identifica unicamente uma requisição
     * @param userId         Identificador do usuário (UUID)
     * @return {@code true} se existir uma transação com a chave de idempotência para o usuário,
     *         {@code false} caso contrário
     */
    boolean existsByIdempotencyKeyAndUserId(UUID idempotencyKey, UUID userId);

    /**
     * Calcula a soma total dos valores de transações do tipo {@code RECEITA} de um usuário
     * dentro de um intervalo de datas.
     *
     * @param userId    Identificador do usuário (UUID)
     * @param startDate Data e hora inicial do período (inclusiva)
     * @param endDate   Data e hora final do período (inclusiva)
     * @return Soma dos valores das receitas no período. Retorna {@link BigDecimal#ZERO} se não houver registros.
     */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.user.id = :userId
            AND t.type = 'RECEITA'
            AND t.createdAt BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumIncomesByPeriod(@Param("userId") UUID userId,
                                  @Param("startDate")LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate
    );

    /**
     * Calcula a soma total dos valores de transações do tipo {@code DESPESA} de um usuário
     * dentro de um intervalo de datas.
     *
     * @param userId    Identificador do usuário (UUID)
     * @param startDate Data e hora inicial do período (inclusiva)
     * @param endDate   Data e hora final do período (inclusiva)
     * @return Soma dos valores das despesas no período. Retorna {@link BigDecimal#ZERO} se não houver registros.
     */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.user.id = :userId
            AND t.type = 'DESPESA'
            AND t.createdAt BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumExpensesByPeriod(@Param("userId") UUID userId,
                                  @Param("startDate")LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate
    );

    /**
     * Agrupa e soma os valores das despesas de um usuário por categoria dentro de um período.
     * <p>
     * O resultado é uma lista de objetos {@link CategorySum}, onde cada elemento contém
     * o nome da categoria, a cor associada e o valor total gasto naquela categoria.
     * </p>
     *
     * @param userId    Identificador do usuário (UUID)
     * @param startDate Data e hora inicial do período (inclusiva)
     * @param endDate   Data e hora final do período (inclusiva)
     * @return Lista de {@link CategorySum} com o total de despesas por categoria no período.
     *         Pode ser vazia se não houver despesas.
     */
    @Query("""
           SELECT new com.gustavosdaniel.myfinance_api.dashboard.CategorySum(
                      c.name, 
                      c.color, 
                      SUM(t.amount)
           )
           FROM Transaction t 
           JOIN t.category c
           WHERE t.user.id = :userId
           AND t.type = 'DESPESA'
           AND t.createdAt BETWEEN :startDate AND :endDate
           GROUP BY c.name, c.color
           """)
    List<CategorySum> sumExpensesByCategoryAndPeriod(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}
