package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.controller.openApi.GoalOpenApi;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
public class GoalController implements GoalOpenApi {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody @Valid GoalRequest request
    ){

        return goalService.createGoal(principal, request);

    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getById(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable UUID id
            ){

        return goalService.getGoalById(id, principal);
    }

    @GetMapping("/search")
    public ResponseEntity<List<GoalResponse>> searchName(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam String name
    ){

        return goalService.searchGoal(principal, name);
    }

    @GetMapping
    public ResponseEntity<Page<GoalResponse>> getAll(
            @AuthenticationPrincipal OAuth2User principal,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String status
            ){

        return goalService.getAllGoals(principal, status, pageable);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GoalResponse> update(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody @Valid GoalRequestUpdate requestUpdate,
            @PathVariable UUID id
    ){

        return goalService.updateGoal(id, requestUpdate, principal);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<GoalResponse> deposit(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody @Valid GoalTransfer transfer
    ){

        return goalService.depositToGoal(id,transfer,principal);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<GoalResponse> withdraw(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody @Valid GoalTransfer transfer
    ) {

        return goalService.withdrawFromGoal(id, transfer, principal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal OAuth2User principal
    ){
        return goalService.deleteGoal(id, principal);
    }
}
