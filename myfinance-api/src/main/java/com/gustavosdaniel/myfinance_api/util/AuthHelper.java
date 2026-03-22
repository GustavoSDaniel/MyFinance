package com.gustavosdaniel.myfinance_api.util;

import com.gustavosdaniel.myfinance_api.controller.metrics.UserMetrics;
import com.gustavosdaniel.myfinance_api.domain.dto.UserRequest;
import com.gustavosdaniel.myfinance_api.domain.enuns.UserRole;
import com.gustavosdaniel.myfinance_api.domain.mapping.UserMapper;
import com.gustavosdaniel.myfinance_api.exception.UnauthorizedException;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthHelper {

    private final UserMetrics userMetrics;
    @Value("${app.security.admin-emails}")
    private List<String> adminEmails;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthHelper(UserRepository userRepository, UserMapper userMapper, UserMetrics userMetrics) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userMetrics = userMetrics;
    }


    public User getCurrentUser(Jwt jwt){
        if (jwt == null){
            throw new UnauthorizedException();
        }

        String keycloakId = jwt.getSubject();

        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> createUserFromJwt(jwt));
    }

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
