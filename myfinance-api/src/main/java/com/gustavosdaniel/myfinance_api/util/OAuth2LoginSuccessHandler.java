package com.gustavosdaniel.myfinance_api.util;

import com.gustavosdaniel.myfinance_api.domain.mapping.UserMapper;
import com.gustavosdaniel.myfinance_api.domain.dto.UserRequest;
import com.gustavosdaniel.myfinance_api.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserMapper userMapper;
    private final UserService userService;

    public OAuth2LoginSuccessHandler(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
        this.setDefaultTargetUrl("/api/v1/auth/user");
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
