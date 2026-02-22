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

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdempotencyKeyAndUserId(UUID idempotencyKey, UUID userId);

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
