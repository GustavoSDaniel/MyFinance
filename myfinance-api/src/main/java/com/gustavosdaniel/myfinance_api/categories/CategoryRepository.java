package com.gustavosdaniel.myfinance_api.categories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório responsável pelas operações de persistência da entidade {@link Category}.
 * <p>
 * Fornece métodos padronizados do Spring Data JPA e consultas customizadas
 * para gerenciar as categorias financeiras dos usuários.
 * </p>
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Verifica se já existe uma categoria cadastrada com um nome específico e um determinado tipo para um usuário.
     * <p>
     * Ignora diferenças entre letras maiúsculas e minúsculas. Ideal para validação de unicidade
     * durante a criação de uma nova categoria, evitando duplicidades para o mesmo tipo de transação (ex: Receita/Despesa).
     *
     * @param categoryName O nome da categoria a ser verificado.
     * @param userId       O ID do usuário proprietário.
     * @param type         O tipo da categoria (ex: INCOME, EXPENSE).
     * @return true se a categoria já existir, false caso contrário.
     */
    boolean existsByNameIgnoreCaseAndUserIdAndType(String categoryName, UUID userId, CategoryType type);

    /**
     * Busca uma categoria específica pelo seu ID e pelo ID do usuário,
     * garantindo que a categoria retornada realmente pertence ao usuário solicitante.
     *
     * @param id     O ID da categoria a ser buscada.
     * @param userId O ID do usuário proprietário.
     * @return Um {@link Optional} contendo a categoria se encontrada, ou vazio caso não exista ou não pertença ao usuário.
     */
    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Realiza uma busca por categorias de um usuário cujo nome contenha o termo pesquisado,
     * ignorando diferenças entre letras maiúsculas e minúsculas.
     *
     * @param name   O termo (ou parte dele) a ser pesquisado no nome da categoria.
     * @param userId O ID do usuário proprietário.
     * @return Uma lista de categorias que correspondem ao critério de busca.
     */
    @Query("""
            SELECT c FROM Category c
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%') ) 
            AND c.user.id = :userId
            """
    )
    List<Category> searchByName(@Param("name") String name, @Param("userId") UUID userId);

    /**
     * Busca todas as categorias associadas a um determinado usuário.
     *
     * @param userId O ID do usuário proprietário.
     * @return Uma lista contendo todas as categorias cadastradas para o usuário.
     */
    List<Category> findByUserId(UUID userId);

    /**
     * Busca todas as categorias ativas associadas a um determinado usuário.
     *
     * @param userId O ID do usuário proprietário.
     * @return Uma lista contendo as categorias ativas do usuário.
     */
    List<Category> findByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Busca todas as categorias inativas associadas a um determinado usuário.
     *
     * @param userId O ID do usuário proprietário.
     * @return Uma lista contendo as categorias inativas do usuário.
     */
    List<Category> findByUserIdAndIsActiveFalse(UUID userId);

}
