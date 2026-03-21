package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.domain.dto.response.UserInfoResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.UserResponse;
import com.gustavosdaniel.myfinance_api.domain.mapping.UserMapper;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.controller.metrics.UserMetrics;
import com.gustavosdaniel.myfinance_api.controller.openApi.UserOpenApi;
import com.gustavosdaniel.myfinance_api.service.UserService;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Controlador REST responsável por gerenciar as requisições relacionadas aos usuários.
 *
 * <p>Disponibiliza endpoints para consulta de dados do usuário logado, listagem paginada,
 * busca por email ou ID, e deleção de contas de usuários.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserOpenApi {

    private final UserService userService;
    private final AuthHelper authHelper;
    private final UserMapper userMapper;
    private final UserMetrics userMetrics;

    public UserController(UserService userService, AuthHelper authHelper, UserMapper userMapper, UserMetrics userMetrics) {
        this.userService = userService;
        this.authHelper = authHelper;
        this.userMapper = userMapper;
        this.userMetrics = userMetrics;
    }

    /**
     * Recupera as informações resumidas do usuário atualmente autenticado no sistema.
     *
     * @return objeto {@link UserInfoResponse} contendo nome, email e foto do usuário logado
     */
    @GetMapping("/me")
    public UserInfoResponse getCurrentUser(){

        Jwt jwt = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        User user = authHelper.getCurrentUser(jwt);

        return userMetrics.recordCurrent(() -> userMapper.toUserInfoResponse(user));
    }

    /**
     * Retorna uma lista paginada de todos os usuários cadastrados no sistema.
     *
     * @param pageable parâmetros de paginação e ordenação (tamanho padrão: 20, ordenado por nome em ordem crescente)
     * @return um {@link ResponseEntity} contendo a página de {@link UserResponse} com os usuários
     */
    @GetMapping("/allUsers")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable){

        Page<UserResponse> users = userService.getAllUsers(pageable);

        return userMetrics.recordGetAll(() -> ResponseEntity.ok(users));
    }

    /**
     * Busca um usuário específico com base no seu endereço de email.
     *
     * @param email o endereço de email do usuário a ser buscado
     * @return um {@link ResponseEntity} contendo o {@link UserResponse} se encontrado, ou status 404 (Not Found) se não existir
     */
    @GetMapping("/email")
    public ResponseEntity<UserResponse> getEmailByUser(@RequestParam String email){

        Optional<UserResponse> user = userService.getUserByEmail(email);

        return userMetrics.recordGetByEmail(() -> user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()));
    }

    /**
     * Busca um usuário específico com base no seu identificador único (UUID).
     *
     * @param id o identificador único (UUID) do usuário
     * @return um {@link ResponseEntity} contendo o {@link UserResponse} com os dados do usuário
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id){

        UserResponse user = userService.getUserById(id);

        return userMetrics.recordGetById(() -> ResponseEntity.ok(user));
    }

    /**
     * Remove a conta de um usuário do sistema.
     *
     * @param id o identificador único (UUID) do usuário a ser removido
     * @param authentication informações de autenticação e contexto de segurança da requisição atual
     * @return um {@link ResponseEntity} com status 204 (No Content) indicando sucesso na operação
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, Authentication authentication){

        userService.deleteUser(id, authentication);

        return ResponseEntity.noContent().build();
    }
}
