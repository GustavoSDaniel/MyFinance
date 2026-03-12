package com.gustavosdaniel.myfinance_api.user;

import com.gustavosdaniel.myfinance_api.exception.AccessDeniedException;
import com.gustavosdaniel.myfinance_api.exception.UserNotFoundException;
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

    @Value("${app.security.admin-emails}")
    private  List<String> adminEmails;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Cria ou atualiza um usuário autenticado via OAuth.
     *
     * <p>Se o usuário já existir (baseado no email), seus dados são atualizados.
     * Caso contrário, um novo usuário é criado. Caso o email esteja listado
     * como administrador na configuração da aplicação, o usuário receberá
     * a role {@link UserRole#ADMIN}.
     *
     * @param request dados do usuário retornados pelo provedor OAuth
     * @return informações do usuário criado ou atualizado
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public UserInfoResponse createOrUpdateUserFromOAuth(UserRequest request) {

        Optional<User> existingUser = userRepository.findByEmail(request.email());

        if (existingUser.isPresent()) {

            User user = existingUser.get();
            user.setName(request.name());
            user.setPicture(request.picture());

            User userUpdate = userRepository.save(user);

            log.info("Usuário: {} atualizado com sucesso", userUpdate.getName());

            return userMapper.toUserInfoResponse(userUpdate);
        }
        UserRole role = UserRole.USER;

        if (adminEmails.contains(request.email())){

            role = UserRole.ADMIN;
        }

        User newUser = userMapper.toUser(request);
        newUser.setRole(role);
        User savedUser = userRepository.save(newUser);

        log.info("Novo usuário: {} salvo com sucesso", savedUser.getName());

        return userMapper.toUserInfoResponse(savedUser);
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
     * a role de administrador. Administradores podem remover qualquer usuário.
     *
     * @param id identificador do usuário a ser removido
     * @param authentication informações do usuário autenticado
     * @return resposta HTTP indicando o resultado da operação
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteUser(UUID id, Authentication authentication) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ADMIN"));

        if (isAdmin){

            userRepository.deleteById(id);
        }

        String emailLogado = authentication.getName();


        if (!isAdmin && !user.getEmail().equals(emailLogado)){

            log.warn("Usuário {} tentou deletar a conta de {}", emailLogado, user.getEmail());

            throw new AccessDeniedException();
        }


        userRepository.delete(user);

        log.info("Usuário {} deletado com sucesso", user.getName());
    }

}
