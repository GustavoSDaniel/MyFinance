package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.openApi.GoalOpenApi;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import com.gustavosdaniel.myfinance_api.exception.InsufficientBalanceException;
import com.gustavosdaniel.myfinance_api.util.InvalidAmountException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/goals")
public class GoalController implements GoalOpenApi {

    private final GoalService goalService;
    private final AuthHelper authHelper;

    public GoalController(GoalService goalService, AuthHelper authHelper) {
        this.goalService = goalService;
        this.authHelper = authHelper;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalRequest request
    ) throws InvalidAmountException {

        User user = authHelper.getCurrentUser(jwt);

        GoalResponse goal = goalService.createGoal(user, request);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(goal.id())
                .toUri();

        return ResponseEntity.created(uri).body(goal);

    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
            ){

        User user = authHelper.getCurrentUser(jwt);

        GoalResponse goal = goalService.getGoalById(id, user);

        return ResponseEntity.ok(goal);
    }

    @GetMapping("/search")
    public ResponseEntity<List<GoalResponse>> searchName(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String name
    ){

        User user = authHelper.getCurrentUser(jwt);

        List<GoalResponse> goals = goalService.searchGoal(user, name);

        return ResponseEntity.ok(goals);
    }

    @GetMapping
    public ResponseEntity<Page<GoalResponse>> getAll(
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String status
            ){

        User user = authHelper.getCurrentUser(jwt);

        Page<GoalResponse> goals = goalService.getAllGoals(user, status, pageable);

        return ResponseEntity.ok(goals);
    }

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

    @PostMapping("/{id}/deposit")
    public ResponseEntity<GoalResponse> deposit(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalTransfer transfer
    ) throws com.gustavosdaniel.myfinance_api.exception.InvalidAmountException, InsufficientBalanceException, InvalidAmountException {

        User user = authHelper.getCurrentUser(jwt);

        GoalResponse goal = goalService.depositToGoal(id, transfer, user);

        return ResponseEntity.ok(goal);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<GoalResponse> withdraw(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalTransfer transfer
    ) throws com.gustavosdaniel.myfinance_api.exception.InvalidAmountException, InsufficientBalanceException, InvalidAmountException {

        User user = authHelper.getCurrentUser(jwt);

        GoalResponse goal = goalService.withdrawFromGoal(id, transfer, user);

        return ResponseEntity.ok(goal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt){

        User user = authHelper.getCurrentUser(jwt);

        goalService.deleteGoal(id, user);

        return ResponseEntity.noContent().build();
    }
}
