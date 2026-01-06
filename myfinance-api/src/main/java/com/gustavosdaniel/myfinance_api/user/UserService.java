package com.gustavosdaniel.myfinance_api.user;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserInfoResponse createOrUpdateUserFromOAuth(UserRequest request);

    Page<UserResponse> getAllUsers(Pageable pageable);

    Optional<UserResponse>  getUserByEmail(String email);

    UserResponse getUserById(UUID id);

    void deleteUser(UUID id);
}
