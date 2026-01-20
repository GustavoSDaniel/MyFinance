package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountServiceImpl accountService;
    private final AuthHelper authHelper;

    public AccountController(AccountServiceImpl accountService, AuthHelper authHelper) {
        this.accountService = accountService;
        this.authHelper = authHelper;
    }

    @PostMapping()
    @Operation(summary = "Cria uma conta para o usuário logado")
    public ResponseEntity<AccountResponse> createdAccount(
            @Valid @RequestBody AccountRequest request,
            @AuthenticationPrincipal OAuth2User principal,
            UriComponentsBuilder uriBuilder) throws AccountNameDuplicate {

        User currentUser = authHelper.getCurrentUser(principal);

        AccountResponse account = accountService.createAccount(request, currentUser);

        URI uri = uriBuilder
                .path("/api/v1/accounts/{id}")
                .buildAndExpand(account.id())
                .toUri();

        return ResponseEntity.created(uri).body(account);
    }
}
