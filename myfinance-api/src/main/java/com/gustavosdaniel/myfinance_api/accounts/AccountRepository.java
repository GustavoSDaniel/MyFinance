package com.gustavosdaniel.myfinance_api.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByNameIgnoreCaseAndUserId(String accountName, UUID userId);
}
