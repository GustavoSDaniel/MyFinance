package com.gustavosdaniel.myfinance_api.user;

import com.gustavosdaniel.myfinance_api.exception.AccessDeniedException;
import com.gustavosdaniel.myfinance_api.exception.UserNotFoundException;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável pelas regras de negócio relacionadas aos usuários.
 *
 * <p>Esta classe gerencia operações como:
 * <ul>
 * <li>Consulta de usuários</li>
 * <li>Remoção de usuários</li>
 * </ul>
 *
 * <p>Também utiliza cache para melhorar a performance nas consultas.
 */
@Service
@CacheConfig(cacheNames = "users")
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;



    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;

    }

    /**
     * Retorna uma lista paginada de todos os usuários cadastrados.
     *
     * @param pageable informações de paginação e ordenação
     * @return página contendo os usuários encontrados
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        Page<User> users = userRepository.findAll(pageable);

        if (users.isEmpty()){

            log.info("Nenhum usuário encontrado na busca");

            return Page.empty();
        }

        log.info("Todos os usuário encontrados {}", users.getNumberOfElements());

        return users.map(userMapper::toUserResponse);

    }

    /**
     * Busca um usuário pelo email.
     *
     * <p>Utiliza cache para melhorar a performance em consultas repetidas.
     *
     * @param email email do usuário
     * @return dados do usuário caso encontrado
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "#email", unless = "#result == null")
    public Optional<UserResponse> getUserByEmail(String email) {

        Optional<User> user = userRepository.findByEmail(email);

        log.info("Buscando usuário pelo email {}", email);

        if (user.isEmpty()){

            log.warn("Nenhum usuário foi encontrado com esse mail {}", email);

            return Optional.empty();
        }

        log.info("Usuário com o email {}, encontrado com sucesso", user.get().getEmail());

        return user.map(userMapper::toUserResponse);

    }

    /**
     * Busca um usuário pelo identificador único.
     *
     * <p>O resultado da consulta é armazenado em cache para melhorar
     * a performance de acessos repetidos.
     *
     * @param id identificador único do usuário
     * @return dados do usuário encontrado
     * @throws UserNotFoundException caso o usuário não exista
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        log.info("Usuário com o id: {} encontrado com sucesso", id);

        return userMapper.toUserResponse(user);
    }

    /**
     * Remove um usuário do sistema.
     *
     * <p>Um usuário pode remover apenas sua própria conta, a menos que possua
     * a role de administrador (ROLE_ADMIN). Administradores podem remover qualquer usuário.
     * Ao remover um usuário, o cache de usuários é totalmente invalidado.
     *
     * @param id identificador do usuário a ser removido
     * @param authentication informações de autenticação e contexto de segurança do usuário logado que realiza a requisição
     * @throws UserNotFoundException caso o usuário a ser deletado não exista
     * @throws AccessDeniedException caso o usuário logado tente deletar outra conta sem possuir privilégios de administrador
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteUser(UUID id, Authentication authentication) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        String loggedInKeycloakId = authentication.getName();

        if (!isAdmin && !user.getKeycloakId().equals(loggedInKeycloakId)){

            log.warn("Usuário {} tentou deletar a conta do usuário de ID {}",
                    loggedInKeycloakId, id);

            throw new AccessDeniedException();
        }


        userRepository.delete(user);

        log.info("Usuário {} deletado com sucesso", user.getName());
    }

}
