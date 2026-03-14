package com.gustavosdaniel.myfinance_api.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório responsável pelas operações de persistência da entidade {@link Account}.
 * <p>
 * Fornece métodos padronizados do Spring Data JPA e consultas customizadas
 * para gerenciar as contas bancárias ou carteiras dos usuários.
 * </p>
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Verifica se já existe uma conta cadastrada com um nome específico para um determinado usuário.
     * Ignora diferenças entre letras maiúsculas e minúsculas.
     * Ideal para validação de unicidade durante a criação de uma nova conta.
     *
     * @param accountName O nome da conta a ser verificado.
     * @param userId      O ID do usuário proprietário.
     * @return true se a conta já existir, false caso contrário.
     */
    boolean existsByNameIgnoreCaseAndUserId(String accountName, UUID userId);

    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String accountName, UUID userId, UUID id);

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
