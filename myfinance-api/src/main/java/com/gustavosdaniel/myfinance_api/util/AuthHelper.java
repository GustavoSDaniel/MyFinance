package com.gustavosdaniel.myfinance_api.util;

import com.gustavosdaniel.myfinance_api.exception.UnauthorizedException;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    private final UserService userService;

    public AuthHelper(UserService userService) {
        this.userService = userService;
    }

    public User getCurrentUser(Jwt jwt){

        if (jwt == null){
            throw new UnauthorizedException();
        }

        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()){
            throw new UnauthorizedException();
        }

        return userService.findByEmail(email);
    }
}
