package com.gustavosdaniel.myfinance_api.repository;

import com.gustavosdaniel.myfinance_api.domain.po.Account;
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

    /**
     * Verifica se já existe uma conta com o mesmo nome para o usuário,
     * ignorando um ID de conta específico. Utilizado na validação de atualização
     * para garantir que o novo nome não conflite com outra conta existente.
     *
     * @param accountName O nome da conta a ser verificado.
     * @param userId      O ID do usuário proprietário.
     * @param id          O ID da conta que será ignorada na busca.
     * @return true se existir outra conta com o mesmo nome, false caso contrário.
     */
    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String accountName, UUID userId, UUID id);

    /**
     * Busca todas as contas associadas a um determinado usuário.
     *
     * @param userId O ID do usuário proprietário.
     * @return Uma lista contendo todas as contas cadastradas para o usuário.
     */
    List<Account> findByUserId(UUID userId);

    /**
     * Busca todas as contas ativas associadas a um determinado usuário.
     *
     * @param userId O ID do usuário proprietário.
     * @return Uma lista contendo as contas ativas do usuário.
     */
    List<Account> findByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Busca todas as contas inativas associadas a um determinado usuário.
     *
     * @param userId O ID do usuário proprietário.
     * @return Uma lista contendo as contas inativas do usuário.
     */
    List<Account> findByUserIdAndIsActiveFalse(UUID userId);

    /**
     * Busca uma conta específica pelo seu ID e pelo ID do usuário,
     * garantindo que a conta retornada realmente pertence ao usuário solicitante.
     *
     * @param id     O ID da conta a ser buscada.
     * @param userId O ID do usuário proprietário.
     * @return Um {@link Optional} contendo a conta se encontrada, ou vazio caso não exista ou não pertença ao usuário.
     */
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Realiza uma busca por contas de um usuário cujo nome contenha o termo pesquisado,
     * ignorando diferenças entre letras maiúsculas e minúsculas.
     * <p>
     * A busca utiliza {@code LIKE} com o padrão {@code %:name%}, retornando todas as contas
     * que possuem o termo em qualquer parte do nome.
     * </p>
     *
     * @param name   O termo (ou parte dele) a ser pesquisado no nome da conta.
     * @param userId O ID do usuário proprietário.
     * @return Uma lista de contas que correspondem ao critério de busca.
     *         Retorna uma lista vazia se nenhuma conta for encontrada.
     */
    @Query("""
           SELECT a FROM Account a
           WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%') ) 
           AND a.user.id = :userId              
           """
    )
    List<Account> searchByName(@Param("name") String name, @Param("userId") UUID userId);

}
