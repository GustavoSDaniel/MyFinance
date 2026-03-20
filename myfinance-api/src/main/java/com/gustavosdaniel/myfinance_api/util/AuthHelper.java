package com.gustavosdaniel.myfinance_api.util;

import com.gustavosdaniel.myfinance_api.exception.UnauthorizedException;
import com.gustavosdaniel.myfinance_api.metrics.UserMetrics;
import com.gustavosdaniel.myfinance_api.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Componente auxiliar para operações de autenticação e gerenciamento do usuário logado.
 *
 * <p>Responsável por extrair informações do token JWT e recuperar ou registrar
 * o usuário atual no banco de dados de forma automática.
 */
@Component
public class AuthHelper {

    private final UserMetrics userMetrics;
    /**
     * Lista de e-mails configurados nas propriedades da aplicação
     * que devem receber privilégios de administrador (ADMIN).
     */
    @Value("${app.security.admin-emails}")
    private List<String> adminEmails;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthHelper(UserRepository userRepository, UserMapper userMapper, UserMetrics userMetrics) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userMetrics = userMetrics;
    }


    /**
     * Recupera o usuário atualmente autenticado com base no token JWT fornecido.
     *
     * <p>Busca o usuário no banco de dados utilizando o identificador único do Keycloak (subject).
     * Se o usuário não for encontrado (indicando um primeiro acesso), um novo registro
     * será criado e salvo automaticamente utilizando os dados contidos no token.
     *
     * @param jwt o token JWT da requisição atual contendo as credenciais do usuário
     * @return a entidade {@link User} correspondente ao usuário logado
     * @throws UnauthorizedException se o token JWT fornecido for nulo
     */
    public User getCurrentUser(Jwt jwt){

        if (jwt == null){
            throw new UnauthorizedException();
        }

        String keycloakId = jwt.getSubject();

        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> createUserFromJwt(jwt));
    }

    /**
     * Cria e persiste um novo usuário no banco de dados utilizando as informações
     * (claims) do token JWT.
     *
     * <p>O nível de acesso (Role) do usuário é definido de forma dinâmica: se o e-mail extraído
     * do token estiver presente na lista de administradores configurada ({@code adminEmails}),
     * o usuário receberá a role {@link UserRole#ADMIN}; caso contrário,
     * receberá {@link UserRole#USER}.
     *
     * @param jwt o token JWT contendo as informações de perfil do usuário (subject, email, name)
     * @return a entidade {@link User} recém-criada e persistida no banco de dados
     */
    private User createUserFromJwt(Jwt jwt){

        String email = jwt.getClaimAsString("email");

        UserRole role = adminEmails.contains(email)
                ? UserRole.ADMIN
                : UserRole.USER;

        User user = userMapper.toUser(new UserRequest(
                jwt.getSubject(),
                email,
                jwt.getClaimAsString("name")
        ));

        user.setRole(role);

        User userSalved = userRepository.save(user);

        userMetrics.incrementCreated();

        return userSalved;

    }

}
