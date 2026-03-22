package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.controller.metrics.AccountMetrics;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountUpdateRequest;
import com.gustavosdaniel.myfinance_api.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController{

    private final AccountService accountService;
    private final AccountMetrics accountMetrics;

    public AccountController(AccountService accountService, AccountMetrics accountMetrics) {
        this.accountService = accountService;
        this.accountMetrics = accountMetrics;
    }

    @PostMapping()
    public ResponseEntity<AccountResponse> createdAccount(
            @Valid @RequestBody AccountRequest request,
            @AuthenticationPrincipal Jwt jwt){

        return accountService.createAccount(request, jwt);
    }

    @GetMapping()
    public ResponseEntity<List<AccountResponseInfo>> getAllAccounts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status){

        return accountMetrics.recordGetAll(() -> accountService.getAllAccounts(jwt, status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AccountResponseInfo>> searchByName(
            @RequestParam @NotBlank String name,
            @AuthenticationPrincipal Jwt jwt
    ){

        return accountMetrics.recordGetSearch(() -> accountService.searchAccount(name, jwt));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseInfo> getAccountById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt){

        return accountMetrics.recordGetById(() -> accountService.getById(id, jwt));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponseInfo> updateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountUpdateRequest request
    ) {

        return accountService.updateAccount(id, jwt, request);
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<Void> activateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        return accountService.activateAccount(id, jwt);
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        return accountService.deactivateAccount(id, jwt);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        return accountService.deleteAccount(id, jwt);
    }
}
