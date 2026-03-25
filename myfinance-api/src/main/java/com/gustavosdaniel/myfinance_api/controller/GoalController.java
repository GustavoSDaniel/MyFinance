package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.domain.dto.request.GoalRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.GoalRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.request.GoalTransferRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.response.GoalResponse;
import com.gustavosdaniel.myfinance_api.controller.metrics.GoalMetrics;
import com.gustavosdaniel.myfinance_api.controller.openApi.GoalOpenApi;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.exception.TransactionCanceledException;
import com.gustavosdaniel.myfinance_api.service.GoalService;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import com.gustavosdaniel.myfinance_api.exception.InsufficientBalanceException;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST responsável pelo gerenciamento de metas financeiras (Goals) do usuário.
 *
 * <p>Disponibiliza endpoints para criação, consulta, atualização, exclusão,
 * além de operações de depósito e saque vinculadas às metas.</p>
 *
 * <p>Os DTOs utilizados são:
 * <ul>
 *   <li>{@link GoalRequest} – entrada para criação de meta</li>
 *   <li>{@link GoalRequestUpdate} – entrada para atualização de meta</li>
 *   <li>{@link GoalTransferRequest} – entrada para depósito/saque</li>
 *   <li>{@link GoalResponse} – saída com dados da meta</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/goals")
public class GoalController implements GoalOpenApi {

    private final GoalService goalService;
    private final AuthHelper authHelper;
    private final GoalMetrics goalMetrics;

    public GoalController(GoalService goalService, AuthHelper authHelper, GoalMetrics goalMetrics) {
        this.goalService = goalService;
        this.authHelper = authHelper;
        this.goalMetrics = goalMetrics;
    }

    /**
     * Cria uma nova meta financeira para o usuário autenticado.
     *
     * @param jwt     token de autenticação contendo os dados do usuário
     * @param request dados da meta a ser criada
     * @return um {@link ResponseEntity} com status 201 (Created) e a meta criada
     * @throws TransactionCanceledException.InvalidAmountException caso os valores informados sejam inválidos
     */
    @PostMapping
    public ResponseEntity<GoalResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalRequest request
    ){

        User user = authHelper.getCurrentUser(jwt);

        GoalResponse goal = goalService.createGoal(user, request);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(goal.id())
                .toUri();

        return ResponseEntity.created(uri).body(goal);

    }

    /**
     * Busca os detalhes de uma meta específica pelo seu ID.
     *
     * @param jwt token de autenticação contendo os dados do usuário
     * @param id  identificador único da meta
     * @return um {@link ResponseEntity} com os dados da meta encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
            ){

        User user = authHelper.getCurrentUser(jwt);

        GoalResponse goal = goalService.getGoalById(id, user);

        return goalMetrics.recordGetById(() -> ResponseEntity.ok(goal));
    }

    /**
     * Busca metas do usuário autenticado filtrando pelo nome.
     * <p>
     * A busca é case‑insensitive e retorna metas cujo nome contenha o termo informado.
     * Se nenhuma meta for encontrada, retorna uma lista vazia.
     * </p>
     *
     * @param jwt  token de autenticação contendo os dados do usuário
     * @param name termo a ser pesquisado no nome das metas
     * @return um {@link ResponseEntity} com a lista de metas encontradas (pode ser vazia)
     */
    @GetMapping("/search")
    public ResponseEntity<List<GoalResponse>> searchName(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String name
    ){

        User user = authHelper.getCurrentUser(jwt);

        List<GoalResponse> goals = goalService.searchGoal(user, name);

        return goalMetrics.recordSearchName(() -> ResponseEntity.ok(goals));
    }

    /**
     * Lista todas as metas do usuário autenticado, com suporte a paginação e filtro por status.
     * <p>
     * O parâmetro {@code status} é tratado de forma case‑insensitive e aceita os valores:
     * "achieved" (alcançadas), "progress" (em andamento) ou qualquer outro valor para listar todas.
     * </p>
     *
     * @param jwt      token de autenticação contendo os dados do usuário
     * @param pageable configurações de paginação (padrão: ordenado por createdAt decrescente)
     * @param status   filtro opcional pelo status da meta (ex: "achieved", "progress")
     * @return um {@link ResponseEntity} contendo uma página de metas
     */
    @GetMapping
    public ResponseEntity<Page<GoalResponse>> getAll(
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String status
            ){

        User user = authHelper.getCurrentUser(jwt);

        Page<GoalResponse> goals = goalService.getAllGoals(user, status, pageable);

        return goalMetrics.recordGetAll(() -> ResponseEntity.ok(goals));
    }

    /**
     * Atualiza os dados de uma meta existente.
     *
     * @param jwt           token de autenticação contendo os dados do usuário
     * @param requestUpdate dados para atualização da meta
     * @param id            identificador único da meta a ser atualizada
     * @return um {@link ResponseEntity} com os dados atualizados da meta
     */
    @PatchMapping("/{id}")
    public ResponseEntity<GoalResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalRequestUpdate requestUpdate,
            @PathVariable UUID id
    ){
        User user = authHelper.getCurrentUser(jwt);

        GoalResponse goal = goalService.updateGoal(id, requestUpdate, user);

        return ResponseEntity.ok(goal);
    }

    /**
     * Realiza um depósito (transferência de uma conta para a meta) incrementando o valor atual.
     *
     * @param id       identificador único da meta
     * @param jwt      token de autenticação contendo os dados do usuário
     * @param transfer dados da transferência (conta de origem e valor)
     * @return um {@link ResponseEntity} com a meta atualizada
     * @throws InsufficientBalanceException  se a conta de origem não tiver saldo suficiente
     */
    @PostMapping("/{id}/deposit")
    public ResponseEntity<GoalResponse> deposit(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalTransferRequest transfer
    ){

        User user = authHelper.getCurrentUser(jwt);

        GoalResponse goal = goalService.depositToGoal(id, transfer, user);

        return ResponseEntity.ok(goal);
    }

    /**
     * Realiza um saque (transferência da meta para uma conta) decrementando o valor atual.
     *
     * @param id       identificador único da meta
     * @param jwt      token de autenticação contendo os dados do usuário
     * @param transfer dados da transferência (conta de destino e valor)
     * @return um {@link ResponseEntity} com a meta atualizada
     * @throws InsufficientBalanceException  se a meta não tiver saldo suficiente para o saque
     */
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<GoalResponse> withdraw(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalTransferRequest transfer
    ) {

        User user = authHelper.getCurrentUser(jwt);

        GoalResponse goal = goalService.withdrawFromGoal(id, transfer, user);

        return ResponseEntity.ok(goal);
    }

    /**
     * Remove uma meta do sistema.
     *
     * @param id  identificador único da meta a ser removida
     * @param jwt token de autenticação contendo os dados do usuário
     * @return um {@link ResponseEntity} com status 204 (No Content) indicando sucesso
     * @throws IllegalArgumentException se a meta ainda possuir saldo (não pode ser deletada)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt){

        User user = authHelper.getCurrentUser(jwt);

        goalService.deleteGoal(id, user);

        return ResponseEntity.noContent().build();
    }
}
