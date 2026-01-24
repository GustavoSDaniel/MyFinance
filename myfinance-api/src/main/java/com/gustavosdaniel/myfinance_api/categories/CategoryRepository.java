package com.gustavosdaniel.myfinance_api.categories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameIgnoreCaseAndUserIdAndType(String categoryName, UUID userId, CategoryType type);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);
}
