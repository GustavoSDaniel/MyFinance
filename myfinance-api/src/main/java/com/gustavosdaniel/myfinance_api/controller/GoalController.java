package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.controller.metrics.GoalMetrics;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalTransfer;
import com.gustavosdaniel.myfinance_api.service.GoalService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
public class GoalController{

    private final GoalService goalService;
    private final GoalMetrics goalMetrics;

    public GoalController(GoalService goalService, GoalMetrics goalMetrics) {
        this.goalService = goalService;
        this.goalMetrics = goalMetrics;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalRequest request
    ){

        return goalService.createGoal(jwt, request);

    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
            ){

        return goalMetrics.recordGetById(() -> goalService.getGoalById(id, jwt));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GoalResponse>> searchName(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String name
    ){

        return goalMetrics.recordSearchName(() -> goalService.searchGoal(jwt, name));
    }

    @GetMapping
    public ResponseEntity<Page<GoalResponse>> getAll(
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String status
            ){

        return goalMetrics.recordGetAll(() -> goalService.getAllGoals(jwt, status, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GoalResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalRequestUpdate requestUpdate,
            @PathVariable UUID id
    ){

        return goalService.updateGoal(id, requestUpdate, jwt);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<GoalResponse> deposit(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalTransfer transfer
    ){

        return goalService.depositToGoal(id,transfer,jwt);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<GoalResponse> withdraw(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoalTransfer transfer
    ) {

        return goalService.withdrawFromGoal(id, transfer, jwt);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal Jwt jwt
    ){
        return goalService.deleteGoal(id, jwt);
    }
}
