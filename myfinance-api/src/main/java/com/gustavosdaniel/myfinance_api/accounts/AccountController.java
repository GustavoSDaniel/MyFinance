package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AuthHelper authHelper;

    public AccountController(AccountServiceImpl accountService, AuthHelper authHelper) {
        this.accountService = accountService;
        this.authHelper = authHelper;
    }

    @PostMapping()
    @Operation(summary = "Cria uma conta para o usuário logado")
    public ResponseEntity<AccountResponse> createdAccount(
            @Valid @RequestBody AccountRequest request,
            @AuthenticationPrincipal OAuth2User principal) throws AccountNameDuplicate {

        User currentUser = authHelper.getCurrentUser(principal);

        AccountResponse account = accountService.createAccount(request, currentUser);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(account.id())
                .toUri();

        return ResponseEntity.created(uri).body(account);
    }

    @GetMapping()
    @Operation(summary = "Mostra todas as contas do usuário logado")
    public ResponseEntity<List<AccountResponseInfo>> getAllAccounts(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(required = false) String status){

        User curentUser = authHelper.getCurrentUser(principal);

        List<AccountResponseInfo> accounts = accountService.getAllAccounts(curentUser.getId(), status);

        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/search")
    @Operation(summary = "Busca conta através do nome da conta")
    public ResponseEntity<List<AccountResponseInfo>> searchByName(
            @RequestParam @NotBlank String name,
            @AuthenticationPrincipal OAuth2User principal
    ){

        User user = authHelper.getCurrentUser(principal);

        List<AccountResponseInfo> account = accountService.searchAccount(name, user.getId());

        return ResponseEntity.ok(account);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca a conta pelo ID")
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
    ) throws AccountNameDuplicate {

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

        accountService.deleteAccount(id, user.getId());

        return ResponseEntity.noContent().build();
    }
}
