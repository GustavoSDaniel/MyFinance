package com.gustavosdaniel.myfinance_api.util;

import com.gustavosdaniel.myfinance_api.exception.UnauthorizedException;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    private final UserService userService;

    public AuthHelper(UserService userService) {
        this.userService = userService;
    }

    public User getCurrentUser(OAuth2User principal){
        if (principal == null){
            throw new UnauthorizedException();
        }

        String email = principal.getAttribute("email");

        return userService.findByEmail(email);
    }
}
