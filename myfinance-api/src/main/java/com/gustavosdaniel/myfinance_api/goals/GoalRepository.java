package com.gustavosdaniel.myfinance_api.goals;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório responsável pelas operações de persistência da entidade {@link Goal} (Meta).
 * <p>
 * Fornece métodos padronizados do Spring Data JPA e consultas customizadas
 * para gerenciar os objetivos financeiros dos usuários.
 * </p>
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {

    /**
     * Verifica se já existe uma meta cadastrada com um nome específico para um determinado usuário.
     * <p>
     * Ignora diferenças entre letras maiúsculas e minúsculas. Ideal para validação de unicidade
     * durante a criação de uma nova meta.
     *
     * @param name   O nome da meta a ser verificado.
     * @param userId O ID do usuário proprietário.
     * @return true se a meta já existir, false caso contrário.
     */
    boolean existsByNameIgnoreCaseAndUserId(String name, UUID userId);

    /**
     * Verifica se já existe uma meta com o mesmo nome para o usuário,
     * ignorando um ID de meta específico. Utilizado na validação de atualização
     * para garantir que o novo nome não conflite com outra meta existente.
     *
     * @param name   O nome da meta a ser verificado.
     * @param userId O ID do usuário proprietário.
     * @param id     O ID da meta que será ignorada na busca.
     * @return true se existir outra meta com o mesmo nome, false caso contrário.
     */
    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String name, UUID userId, UUID id);

    /**
     * Busca uma meta específica pelo seu ID e pelo ID do usuário,
     * garantindo que a meta retornada realmente pertence ao usuário solicitante.
     *
     * @param id     O ID da meta a ser buscada.
     * @param userId O ID do usuário proprietário.
     * @return Um {@link Optional} contendo a meta se encontrada, ou vazio caso não exista ou não pertença ao usuário.
     */
    Optional<Goal> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Retorna uma lista paginada com todas as metas associadas a um determinado usuário.
     *
     * @param userId   O ID do usuário proprietário.
     * @param pageable Configurações de paginação e ordenação.
     * @return Uma página contendo as metas do usuário.
     */
    Page<Goal> findByUserId(UUID userId, Pageable pageable);

    /**
     * Realiza uma busca por metas de um usuário cujo nome contenha o termo pesquisado,
     * ignorando diferenças entre letras maiúsculas e minúsculas.
     *
     * @param name   O termo (ou parte dele) a ser pesquisado no nome da meta.
     * @param userId O ID do usuário proprietário.
     * @return Uma lista de metas que correspondem ao critério de busca.
     */
    @Query("""
            SELECT g FROM Goal g
            WHERE lower(g.name) LIKE LOWER(CONCAT('%', :name, '%' ) )
            AND g.user.id = :userId
            """)
    List<Goal> searchName(@Param("name") String name, @Param("userId") UUID userId);

    /**
     * Busca todas as metas já alcançadas por um usuário (onde o valor atual é maior ou igual ao valor alvo),
     * retornando os resultados de forma paginada.
     *
     * @param userId   O ID do usuário proprietário.
     * @param pageable Configurações de paginação e ordenação.
     * @return Uma página contendo as metas alcançadas.
     */
    @Query("""
            SELECT g FROM Goal g WHERE g.user.id = :userId AND g.currentAmount >= g.targetAmount
            """)
    Page<Goal> findAchievedGoals(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Busca todas as metas pendentes de um usuário (onde o valor atual é menor que o valor alvo),
     * retornando os resultados de forma paginada.
     *
     * @param userId   O ID do usuário proprietário.
     * @param pageable Configurações de paginação e ordenação.
     * @return Uma página contendo as metas pendentes.
     */
    @Query("""
            SELECT g FROM Goal g WHERE g.user.id = :userId AND g.currentAmount < g.targetAmount
            """)
    Page<Goal> findPendingGoals(@Param("userId") UUID userId, Pageable pageable);


}
