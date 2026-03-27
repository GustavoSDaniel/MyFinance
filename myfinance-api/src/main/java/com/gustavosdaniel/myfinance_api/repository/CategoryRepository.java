package com.gustavosdaniel.myfinance_api.repository;

import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.domain.enuns.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameIgnoreCaseAndUserIdAndType(String categoryName, UUID userId, CategoryType type);

    boolean existsByNameIgnoreCaseAndUserIdAndTypeAndIdNot(String categoryName, UUID userId, CategoryType type, UUID categoryId);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
            SELECT c FROM Category c
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%') ) 
            AND c.user.id = :userId
            """
    )
    List<Category> searchByName(@Param("name") String name, @Param("userId") UUID userId);

    List<Category> findByUserId(UUID userId);

    List<Category> findByUserIdAndIsActiveTrue(UUID userId);

    List<Category> findByUserIdAndIsActiveFalse(UUID userId);



}
