package com.gustavosdaniel.myfinance_api.repository;

import com.gustavosdaniel.myfinance_api.domain.po.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {

    boolean existsByNameIgnoreCaseAndUserId(String name, UUID userId);

    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String name, UUID userId, UUID id);

    Optional<Goal> findByIdAndUserId(UUID id, UUID userId);

    Page<Goal> findByUserId(UUID userId, Pageable pageable);

    @Query("""
            SELECT g FROM Goal g
            WHERE lower(g.name) LIKE LOWER(CONCAT('%', :name, '%' ) )
            AND g.user.id = :userId
            """)
    List<Goal> searchName(@Param("name") String name, @Param("userId") UUID userId);

    @Query("""
            SELECT g FROM Goal g WHERE g.user.id = :userId AND g.currentAmount >= g.targetAmount
            """)
    Page<Goal> findAchievedGoals(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            SELECT g FROM Goal g WHERE g.user.id = :userId AND g.currentAmount < g.targetAmount
            """)
    Page<Goal> findPendingGoals(@Param("userId") UUID userId, Pageable pageable);


}
