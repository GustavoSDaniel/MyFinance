package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.controller.metrics.TransactionMetrics;
import com.gustavosdaniel.myfinance_api.domain.dto.TransactionRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.TransactionResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.TransactionSearchFilter;
import com.gustavosdaniel.myfinance_api.domain.dto.TransferRequest;
import com.gustavosdaniel.myfinance_api.service.TransactionService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController{

    private final TransactionService transactionService;
    private final TransactionMetrics transactionMetrics;

    public TransactionController(TransactionService transactionService, TransactionMetrics transactionMetrics) {
        this.transactionService = transactionService;
        this.transactionMetrics = transactionMetrics;
    }


    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody @Valid TransactionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){

        return transactionService.createTransaction(jwt, request);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Void> confirmTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        return transactionService.transactionConfirmed(id, jwt);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        return transactionService.transactionCancel(id, jwt);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @RequestBody @Valid TransferRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){

        return transactionService.transfer(jwt, request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        return transactionMetrics.recordGetById(() -> transactionService.getTransactionById(id, jwt));

    }

    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponse>> allTransactionsWithFilter(

            @ParameterObject TransactionSearchFilter filter,
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return transactionMetrics.recordGetAll(() -> transactionService.getAllWithFilter(jwt, filter, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(

            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {

        return transactionService.deleteTransaction(id, jwt);
    }
}
