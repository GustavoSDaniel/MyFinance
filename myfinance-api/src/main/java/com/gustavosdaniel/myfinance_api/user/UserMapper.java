package com.gustavosdaniel.myfinance_api.user;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(UserRequest request) {

        if (request == null) {
            return null;
        }

        return new User(
                request.email(),
                request.name(),
                UserRole.ROLE_USER
        );
    }

    public UserRequest toUserRequest(OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return null;
        }

        return new UserRequest(
                oAuth2User.getAttribute("email"),
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("picture")
        );
    }

    public UserInfoResponse toUserInfoResponse(User user) {

        if (user == null) {
            return null;
        }

        return new UserInfoResponse(
                user.getName(),
                user.getEmail(),
                user.getPicture()
        );
    }
}
