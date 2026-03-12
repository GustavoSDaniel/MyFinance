package com.gustavosdaniel.myfinance_api.user;

import com.gustavosdaniel.myfinance_api.openApi.UserOpenApi;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserOpenApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/allUsers")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable){

        Page<UserResponse> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponse> getEmailByUser(@RequestParam String email){

        Optional<UserResponse> user = userService.getUserByEmail(email);

        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id){

        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(user);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, Authentication authentication){

        userService.deleteUser(id, authentication);

        return ResponseEntity.noContent().build();
    }
}
