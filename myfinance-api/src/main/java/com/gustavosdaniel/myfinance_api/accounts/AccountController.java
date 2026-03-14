package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.exception.AccountNameDuplicateException;
import com.gustavosdaniel.myfinance_api.openApi.AccountOpenApi;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController implements AccountOpenApi {

    private final AccountService accountService ;
    private final AuthHelper authHelper;

    public AccountController(AccountService accountService, AuthHelper authHelper) {
        this.accountService = accountService;
        this.authHelper = authHelper;
    }

    @PostMapping()
    public ResponseEntity<AccountResponse> createdAccount(
            @Valid @RequestBody AccountRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        User currentUser = authHelper.getCurrentUser(jwt);

        AccountResponse account = accountService.createAccount(request, currentUser);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(account.id())
                .toUri();

        return ResponseEntity.created(uri).body(account);
    }

    @GetMapping()
    public ResponseEntity<List<AccountResponseInfo>> getAllAccounts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status){

        User curentUser = authHelper.getCurrentUser(jwt);

        List<AccountResponseInfo> accounts = accountService.getAllAccounts(curentUser.getId(), status);

        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/search")
    public ResponseEntity<List<AccountResponseInfo>> searchByName(
            @RequestParam @NotBlank String name,
            @AuthenticationPrincipal Jwt jwt
    ){

        User user = authHelper.getCurrentUser(jwt);

        List<AccountResponseInfo> account = accountService.searchAccount(name, user.getId());

        return ResponseEntity.ok(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseInfo> getAccountById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt){

        User curentUser = authHelper.getCurrentUser(jwt);

        AccountResponseInfo account = accountService.getById(id, curentUser.getId());

        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponseInfo> updateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountUpdateRequest request
    ) throws AccountNameDuplicateException {

        User user = authHelper.getCurrentUser(jwt);

        AccountResponseInfo account = accountService.updateAccount(id, user.getId(), request);

        return ResponseEntity.ok(account);
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<Void> activateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){
        User user = authHelper.getCurrentUser(jwt);

        accountService.activateAccount(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){
        User user = authHelper.getCurrentUser(jwt);

        accountService.deactivateAccount(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){
        User user = authHelper.getCurrentUser(jwt);

        accountService.deleteAccount(id, user);

        return ResponseEntity.noContent().build();
    }
}
