package com.gustavosdaniel.myfinance_api.repository;

import com.gustavosdaniel.myfinance_api.domain.po.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


/**
 * Repositório para a entidade {@link User}, responsável pelas operações de persistência.
 * <p>
 * Fornece métodos para acesso a dados de usuário, incluindo consultas por email
 * e por identificador do Keycloak.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Busca um usuário pelo endereço de e-mail.
     * <p>
     * <strong>Nota:</strong> A busca é sensível a maiúsculas/minúsculas (case-sensitive),
     * a menos que a configuração do banco de dados utilize uma collation case-insensitive.
     * </p>
     *
     * @param email O e-mail do usuário a ser pesquisado.
     * @return Um {@link Optional} contendo o usuário correspondente, ou vazio se não for encontrado.
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca um usuário pelo seu identificador único no Keycloak.
     * <p>
     * O Keycloak ID é uma string única fornecida pelo provedor de autenticação
     * e serve como chave de ligação entre o sistema local e o usuário autenticado.
     * </p>
     *
     * @param keycloakId O ID do usuário no Keycloak.
     * @return Um {@link Optional} contendo o usuário correspondente, ou vazio se não for encontrado.
     */
    Optional<User> findByKeycloakId(String keycloakId);
}
