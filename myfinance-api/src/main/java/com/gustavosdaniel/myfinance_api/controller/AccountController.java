package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.controller.openapi.AccountOpenApi;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountUpdateRequest;
import com.gustavosdaniel.myfinance_api.service.AccountService;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
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
    //TODO remover e colocar dentro do service
    private final AuthHelper authHelper;

    public AccountController(AccountService accountService, AuthHelper authHelper) {
        this.accountService = accountService;
        this.authHelper = authHelper;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createdAccount(@Valid @RequestBody AccountRequest request,
                                                          @AuthenticationPrincipal OAuth2User principal) {
        return accountService.createAccount(principal, request);
    }

    @GetMapping()
    public ResponseEntity<List<AccountResponseInfo>> getAllAccounts(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(required = false) String status){

        User curentUser = authHelper.getCurrentUser(principal);

        List<AccountResponseInfo> accounts = accountService.getAllAccounts(curentUser.getId(), status);

        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/search")
    public ResponseEntity<List<AccountResponseInfo>> searchByName(
            @RequestParam @NotBlank String name,
            @AuthenticationPrincipal OAuth2User principal
    ){

        User user = authHelper.getCurrentUser(principal);

        List<AccountResponseInfo> account = accountService.searchAccount(name, user.getId());

        return ResponseEntity.ok(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseInfo> getAccountById(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal){

        User curentUser = authHelper.getCurrentUser(principal);

        AccountResponseInfo account = accountService.getById(id, curentUser.getId());

        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizando informações da conta")
    public ResponseEntity<AccountResponseInfo> updateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody AccountUpdateRequest request
    ) {

        User user = authHelper.getCurrentUser(principal);

        AccountResponseInfo account = accountService.updateAccount(id, user.getId(), request);

        return ResponseEntity.ok(account);
    }

    @PatchMapping("/activate/{id}")
    @Operation(summary = "Ativa a conta que está desativada")
    public ResponseEntity<Void> activateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){
        User user = authHelper.getCurrentUser(principal);

        accountService.activateAccount(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/deactivate/{id}")
    @Operation(summary = "Desativa a conta que está ativada")
    public ResponseEntity<Void> deactivateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){
        User user = authHelper.getCurrentUser(principal);

        accountService.deactivateAccount(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta conta do usuário")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){
        User user = authHelper.getCurrentUser(principal);

        accountService.deleteAccount(id, user);

        return ResponseEntity.noContent().build();
    }
}
