package com.gustavosdaniel.myfinance_api.user;

import io.swagger.v3.oas.annotations.Operation;
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
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/allUsers")
    @Operation(summary = "Mostra todos os usuários")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable){

        Page<UserResponse> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/email")
    @Operation(summary = "Busca usuário pelo email")
    public ResponseEntity<UserResponse> getEmailByUser(@RequestParam String email){

        Optional<UserResponse> user = userService.getUserByEmail(email);

        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca o usuário pelo id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id){

        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta usuário pelo ID")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, Authentication authentication){

        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

        if (isAdmin){
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        }

        String emailLogado = authentication.getName();
        UserResponse user = userService.getUserById(id);

        if (!user.email().equals(emailLogado)){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
