package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.controller.openApi.AccountOpenApi;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountUpdateRequest;
import com.gustavosdaniel.myfinance_api.service.AccountService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController implements AccountOpenApi {

    private final AccountService accountService;

    private final Counter createAccountCounter;
    private final Counter deleteAccountCounter;
    private final Timer getAccountTimer;

    public AccountController(AccountService accountService, MeterRegistry registry) {
        this.accountService = accountService;

        this.createAccountCounter = registry.counter("accounts.created");
        this.deleteAccountCounter = registry.counter("accounts.deleted");
        this.getAccountTimer = registry.timer("accounts.get.by.id.latency");
    }


    @PostMapping()
    public ResponseEntity<AccountResponse> createdAccount(
            @Valid @RequestBody AccountRequest request,
            @AuthenticationPrincipal OAuth2User principal){

        createAccountCounter.increment();

        return accountService.createAccount(request, principal);
    }

    @GetMapping()
    public ResponseEntity<List<AccountResponseInfo>> getAllAccounts(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(required = false) String status){

        return accountService.getAllAccounts(principal, status);
    }

    @GetMapping("/search")
    public ResponseEntity<List<AccountResponseInfo>> searchByName(
            @RequestParam @NotBlank String name,
            @AuthenticationPrincipal OAuth2User principal
    ){

        return accountService.searchAccount(name, principal);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseInfo> getAccountById(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal){

        return getAccountTimer.record(() ->accountService.getById(id, principal));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponseInfo> updateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody AccountUpdateRequest request
    ) {

        return accountService.updateAccount(id, principal, request);
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<Void> activateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        return accountService.activateAccount(id, principal);
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        return accountService.deactivateAccount(id, principal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        return accountService.deleteAccount(id, principal);
    }
}
