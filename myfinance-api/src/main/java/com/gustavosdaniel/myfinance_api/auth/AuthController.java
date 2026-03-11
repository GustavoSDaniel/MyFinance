package com.gustavosdaniel.myfinance_api.auth;

import com.gustavosdaniel.myfinance_api.openApi.AuthOpenApi;
import com.gustavosdaniel.myfinance_api.user.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthOpenApi {

    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal OAuth2User principal) {

        UserInfoResponse user = new UserInfoResponse(
                principal.getAttribute("name"),
                principal.getAttribute("email"),
                principal.getAttribute("picture")
        );

        return ResponseEntity.ok(user);

    }
}
