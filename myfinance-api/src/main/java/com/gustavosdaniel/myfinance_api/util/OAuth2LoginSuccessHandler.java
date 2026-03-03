package com.gustavosdaniel.myfinance_api.util;

import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserMapper;
import com.gustavosdaniel.myfinance_api.user.UserRequest;
import com.gustavosdaniel.myfinance_api.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final UserMapper userMapper;


    public OAuth2LoginSuccessHandler(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.setDefaultTargetUrl("/api/v1/dashboards");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        UserRequest userRequest = userMapper.toUserRequest(oAuth2User);

        userService.createOrUpdateUserFromOAuth(userRequest);

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
