package com.gustavosdaniel.myfinance_api.service;


import com.gustavosdaniel.myfinance_api.domain.dto.UserInfoResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.UserResponse;
import com.gustavosdaniel.myfinance_api.domain.mapping.UserMapper;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.exception.UserNotFoundException;
import com.gustavosdaniel.myfinance_api.repository.UserRepository;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável pelas regras de negócio relacionadas aos usuários.
 *
 * <p>Esta classe gerencia operações como:
 * <ul>
 *     <li>Criação ou atualização de usuários via OAuth</li>
 *     <li>Consulta de usuários</li>
 *     <li>Remoção de usuários</li>
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
    private final AuthHelper authHelpe;

    public UserService(UserRepository userRepository, UserMapper userMapper, AuthHelper authHelpe) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.authHelpe = authHelpe;
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#jwt.subject")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Jwt jwt){

        User user = authHelpe.getCurrentUser(jwt);

        UserInfoResponse response = userMapper.toUserInfoResponse(user);

        return ResponseEntity.ok(response);
    }


    /**
     * Retorna uma lista paginada de todos os usuários cadastrados.
     *
     * @param pageable informações de paginação e ordenação
     * @return página contendo os usuários encontrados
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {

        Page<User> users = userRepository.findAll(pageable);

        if (users.isEmpty()){

            log.info("Nenhum usuário encontrado na busca");

            return ResponseEntity.noContent().build();
        }

        log.info("Todos os usuário encontrados {}", users.getNumberOfElements());

        return ResponseEntity.ok(users.map(userMapper::toUserResponse));

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
    public ResponseEntity<UserResponse> getUserByEmail(String email) {

        Optional<User> user = userRepository.findByEmail(email);

        log.info("Buscando usuário pelo email {}", email);

        if (user.isEmpty()){

            log.warn("Nenhum usuário foi encontrado com esse mail {}", email);

            return ResponseEntity.noContent().build();
        }

        log.info("Usuário com o email {}, encontrado com sucesso", user.get().getEmail());

        UserResponse response = userMapper.toUserResponse(user.get());

        return ResponseEntity.ok(response);

    }

    /**
     * Busca um usuário pelo email.
     *
     * <p>Este método lança uma exceção caso o usuário não seja encontrado.
     *
     * @param email email do usuário
     * @return entidade {@link User}
     * @throws UserNotFoundException caso o usuário não exista
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {

        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    /**
     * Busca um usuário pelo ID.
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
    public ResponseEntity<UserResponse> getUserById(UUID id) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        log.info("Usuário com o id: {} encontrado com sucesso", id);

        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    /**
     * Remove um usuário do sistema.
     *
     * <p>Um usuário pode remover apenas sua própria conta, a menos que possua
     * a role de administrador. Administradores podem remover qualquer usuário.
     *
     * @param id ID do usuário a ser removido
     * @param authentication informações do usuário autenticado
     * @return resposta HTTP indicando o resultado da operação
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deleteUser(UUID id, Authentication authentication) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ADMIN"));

        if (isAdmin){

            userRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        }

        String emailLogado = authentication.getName();

        if (!isAdmin && !user.getEmail().equals(emailLogado)) {

            log.warn("Usuário {} tentou deletar a conta de {}", emailLogado, user.getEmail());

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userRepository.delete(user);

        log.info("Usuário {} deletado com sucesso", user.getName());

        return ResponseEntity.noContent().build();
    }

}
