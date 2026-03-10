package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.domain.dto.TransactionRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.TransactionResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.TransactionSearchFilter;
import com.gustavosdaniel.myfinance_api.domain.dto.TransferRequest;
import com.gustavosdaniel.myfinance_api.service.TransactionService;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthHelper authHelper;

    public TransactionController(TransactionService transactionService, AuthHelper authHelper) {
        this.transactionService = transactionService;
        this.authHelper = authHelper;
    }


    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody @Valid TransactionRequest request,
            @AuthenticationPrincipal OAuth2User principal
    ){

        return transactionService.createTransaction(principal, request);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Void> confirmTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        return transactionService.transactionConfirmed(id, principal);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        return transactionService.transactionCancel(id, principal);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @RequestBody @Valid TransferRequest request,
            @AuthenticationPrincipal OAuth2User principal
    ){

        return transactionService.transfer(principal, request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        return transactionService.getTransactionById(id, principal);

    }

    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponse>> allTransactionsWithFilter(

            @ParameterObject TransactionSearchFilter filter,
            @AuthenticationPrincipal OAuth2User principal,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return transactionService.getAllWithFilter(principal, filter, pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(

            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ) {

        return transactionService.deleteTransaction(id, principal);
    }
}
