package com.gustavosdaniel.myfinance_api.domain.mapping;

import com.gustavosdaniel.myfinance_api.domain.dto.UserInfoResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.UserRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.UserResponse;
import com.gustavosdaniel.myfinance_api.domain.enuns.UserRole;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(UserRequest request) {

        if (request == null) {
            return null;
        }

        return new User(
                request.keycloakId(),
                request.email(),
                request.name(),
                UserRole.USER
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

    public UserResponse toUserResponse(User user){
        if (user == null){
            return null;
        }

        return new UserResponse(user.getId(),user.getName(), user.getEmail());
    }
}
