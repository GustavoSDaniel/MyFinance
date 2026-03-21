package com.gustavosdaniel.myfinance_api.repository;

import com.gustavosdaniel.myfinance_api.domain.po.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para a entidade User, responsável pelas operações de persistência.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Busca um usuário pelo endereço de e-mail.
     *
     * @param email O e-mail do usuário a ser pesquisado.
     * @return Um {@link Optional} contendo o usuário correspondente, ou vazio se não for encontrado.
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca um usuário pelo seu identificador único no Keycloak.
     *
     * @param keycloakId O ID do usuário no Keycloak.
     * @return Um {@link Optional} contendo o usuário correspondente, ou vazio se não for encontrado.
     */
    Optional<User> findByKeycloakId(String keycloakId);
}
