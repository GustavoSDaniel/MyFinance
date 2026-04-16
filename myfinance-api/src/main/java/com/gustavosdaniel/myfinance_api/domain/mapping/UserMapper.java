package com.gustavosdaniel.myfinance_api.domain.mapping;

import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.domain.dto.response.UserInfoResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.request.UserRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.response.UserResponse;
import com.gustavosdaniel.myfinance_api.domain.enuns.UserRole;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Componente responsável pela conversão e mapeamento de objetos relacionados à entidade {@link User}.
 * <p>
 * Fornece métodos para:
 * <ul>
 *   <li>Criar uma entidade {@link User} a partir de um {@link UserRequest};</li>
 *   <li>Converter uma entidade {@link User} em DTOs de resposta ({@link UserInfoResponse} e {@link UserResponse}).</li>
 * </ul>
 * </p>
 */
@Component
public class UserMapper {

    private final Predicate<Object> isNull = Objects::isNull;

    /**
     * Converte um {@link UserRequest} em uma entidade {@link User}.
     *
     * @param request O objeto de transferência de dados com as informações do usuário.
     * @return Uma nova instância de {@link User}, ou {@code null} se o request for nulo.
     */
    public User toUser(UserRequest request) {

        if (isNull.test(request)) {
            return null;
        }

        return new User(
                request.keycloakId(),
                request.email(),
                request.name(),
                UserRole.USER
        );
    }


    /**
     * Converte uma entidade {@link User} em um {@link UserInfoResponse}.
     *
     * @param user A entidade contendo os dados do usuário.
     * @return Uma nova instância de {@link UserInfoResponse}, ou {@code null} se o usuário for nulo.
     */
    public UserInfoResponse toUserInfoResponse(User user) {

        if (isNull.test(user)) {
            return null;
        }

        return new UserInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPicture()
        );
    }

    /**
     * Converte uma entidade {@link User} em um {@link UserResponse}.
     *
     * @param user A entidade contendo os dados do usuário.
     * @return Uma nova instância de {@link UserResponse}, ou {@code null} se o usuário for nulo.
     */
    public UserResponse toUserResponse(User user){
        if (isNull.test(user)){
            return null;
        }

        return new UserResponse(user.getId(),user.getName(), user.getEmail(), user.getRole());
    }

    public User fromKeycloakClaims(String keycloakId, String email, String name, UserRole role){

        if (keycloakId == null || email == null || name == null){
            throw new IllegalArgumentException("KeycloakId, email e nome são obrigatórios");
        }

        return new User( keycloakId,  email,  name,  role);
    }
}
