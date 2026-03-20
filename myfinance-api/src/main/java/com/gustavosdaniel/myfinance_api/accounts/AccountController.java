package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.exception.AccountNameDuplicateException;
import com.gustavosdaniel.myfinance_api.metrics.AccountMetrics;
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

/**
 * Controlador REST responsável por gerenciar as requisições relacionadas às contas (Accounts) dos usuários.
 *
 * <p>Fornece endpoints para criação, listagem, busca, atualização, ativação, desativação
 * e remoção de contas vinculadas ao usuário autenticado.
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController implements AccountOpenApi {

    private final AccountService accountService ;
    private final AuthHelper authHelper;
    private final AccountMetrics accountMetrics;

    public AccountController(AccountService accountService, AuthHelper authHelper, AccountMetrics accountMetrics) {
        this.accountService = accountService;
        this.authHelper = authHelper;
        this.accountMetrics = accountMetrics;
    }

    /**
     * Cria uma nova conta vinculada ao usuário atualmente autenticado.
     *
     * @param request os dados necessários para a criação da conta
     * @param jwt     o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} com status 201 (Created), a URI da nova conta no cabeçalho Location e os dados criados no corpo
     */
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

    /**
     * Retorna uma lista com todas as contas do usuário autenticado, com a opção de filtrar pelo status.
     *
     * @param jwt    o token JWT contendo as credenciais do usuário
     * @param status filtro opcional pelo status da conta (ex: ativa, inativa)
     * @return um {@link ResponseEntity} contendo a lista de {@link AccountResponseInfo} com as contas encontradas
     */
    @GetMapping()
    public ResponseEntity<List<AccountResponseInfo>> getAllAccounts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status){

        User curentUser = authHelper.getCurrentUser(jwt);

        List<AccountResponseInfo> accounts = accountService.getAllAccounts(curentUser.getId(), status);

        return accountMetrics.recordGetAll(() -> ResponseEntity.ok(accounts));
    }

    /**
     * Realiza uma busca por contas do usuário autenticado cujo nome corresponda ao termo informado.
     *
     * @param name o termo ou nome a ser pesquisado (não pode ser nulo ou vazio)
     * @param jwt  o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} contendo a lista de contas que correspondem à busca
     */
    @GetMapping("/search")
    public ResponseEntity<List<AccountResponseInfo>> searchByName(
            @RequestParam @NotBlank String name,
            @AuthenticationPrincipal Jwt jwt
    ){

        User user = authHelper.getCurrentUser(jwt);

        List<AccountResponseInfo> account = accountService.searchAccount(name, user.getId());

        return accountMetrics.recordGetSearch(() -> ResponseEntity.ok(account));
    }

    /**
     * Busca os detalhes de uma conta específica pertencente ao usuário autenticado.
     *
     * @param id  o identificador único (UUID) da conta a ser buscada
     * @param jwt o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} contendo as informações detalhadas da conta
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseInfo> getAccountById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt){

        User curentUser = authHelper.getCurrentUser(jwt);

        AccountResponseInfo account = accountService.getById(id, curentUser.getId());

        return accountMetrics.recordGetById(() -> ResponseEntity.ok(account));
    }

    /**
     * Atualiza os dados de uma conta existente pertencente ao usuário autenticado.
     *
     * @param id      o identificador único (UUID) da conta a ser atualizada
     * @param jwt     o token JWT contendo as credenciais do usuário
     * @param request os novos dados a serem aplicados na conta
     * @return um {@link ResponseEntity} contendo as informações atualizadas da conta
     * @throws AccountNameDuplicateException se o novo nome fornecido já estiver em uso por outra conta do usuário
     */
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

    /**
     * Ativa uma conta previamente inativada pertencente ao usuário autenticado.
     *
     * @param id  o identificador único (UUID) da conta a ser ativada
     * @param jwt o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} com status 204 (No Content) indicando sucesso na operação
     */
    @PatchMapping("/activate/{id}")
    public ResponseEntity<Void> activateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){
        User user = authHelper.getCurrentUser(jwt);

        accountService.activateAccount(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Desativa uma conta pertencente ao usuário autenticado.
     *
     * @param id  o identificador único (UUID) da conta a ser desativada
     * @param jwt o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} com status 204 (No Content) indicando sucesso na operação
     */
    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){
        User user = authHelper.getCurrentUser(jwt);

        accountService.deactivateAccount(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Remove de forma permanente uma conta pertencente ao usuário autenticado.
     *
     * @param id  o identificador único (UUID) da conta a ser removida
     * @param jwt o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} com status 204 (No Content) indicando sucesso na deleção
     */
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
