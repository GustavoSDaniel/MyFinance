package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.controller.metrics.UserMetrics;
import com.gustavosdaniel.myfinance_api.controller.openApi.UserOpenApi;
import com.gustavosdaniel.myfinance_api.domain.dto.response.UserInfoResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.UserResponse;
import com.gustavosdaniel.myfinance_api.service.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserOpenApi {

    private final UserService userService;
    private final UserMetrics userMetrics;

    public UserController(UserService userService, UserMetrics userMetrics) {
        this.userService = userService;
        this.userMetrics = userMetrics;
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(@AuthenticationPrincipal Jwt jwt){

        return userMetrics.recordCurrent(() -> userService.getCurrentUser(jwt));
    }

    @GetMapping("/allUsers")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable){

        return userMetrics.recordGetAll(() -> userService.getAllUsers(pageable));
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponse> getEmailByUser(@RequestParam String email){

        return userMetrics.recordGetByEmail(() -> userService.getUserByEmail(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id){

        return userMetrics.recordGetById(() -> userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, Authentication authentication){

        return userService.deleteUser(id, authentication);
    }
}
