package com.gustavosdaniel.myfinance_api.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByNameIgnoreCaseAndUserId(String accountName, UUID userId);

    List<Account> findByUserId(UUID userId);

    List<Account> findByUserIdAndIsActiveTrue(UUID userId);

    List<Account> findByUserIdAndIsActiveFalse(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
           SELECT a FROM Account a
           WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%') ) 
           AND a.user.id = :userId              
           """
    )
    List<Account> searchByName(@Param("name") String name, @Param("userId") UUID userId);
}
