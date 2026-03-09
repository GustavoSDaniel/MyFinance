package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.exception.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import com.gustavosdaniel.myfinance_api.exception.InsufficientBalanceException;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionServiceImpl transactionService;
    private final AuthHelper authHelper;

    public TransactionController(TransactionServiceImpl transactionService, AuthHelper authHelper) {
        this.transactionService = transactionService;
        this.authHelper = authHelper;
    }


    @PostMapping
    @Operation(summary = "Cria uma transação")
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody @Valid TransactionRequest request,
            @AuthenticationPrincipal OAuth2User principal
    ) throws InvalidAmountException, InsufficientBalanceException {

        User user = authHelper.getCurrentUser(principal);

        TransactionResponse transaction = transactionService.createTransaction(user, request);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transaction.id())
                .toUri();

        return ResponseEntity.created(uri).body(transaction);
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirma uma transação pendente")
    public ResponseEntity<Void> confirmTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ) throws InvalidAmountException, InsufficientBalanceException {

        User user = authHelper.getCurrentUser(principal);

        transactionService.transactionConfirmed(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancela uma transação confirmada")
    public ResponseEntity<Void> cancelTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ) throws InvalidAmountException, InsufficientBalanceException {

        User user = authHelper.getCurrentUser(principal);

        transactionService.transactionCancel(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Realiza transferência entre contas")
    public ResponseEntity<Void> transfer(
            @RequestBody @Valid TransferRequest request,
            @AuthenticationPrincipal OAuth2User principal
    ) throws InvalidAmountException, InsufficientBalanceException {

        User user = authHelper.getCurrentUser(principal);

        transactionService.transfer(user, request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca transação por ID")
    public ResponseEntity<TransactionResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        User user = authHelper.getCurrentUser(principal);

        TransactionResponse transaction = transactionService.getTransactionById(id, user.getId());

        return ResponseEntity.ok(transaction);

    }

    @GetMapping("/search")
    @Operation(summary = "Busca transações com filtros")
    public ResponseEntity<Page<TransactionResponse>> allTransactionsWithFilter(

            @ParameterObject TransactionSearchFilter filter,
            @AuthenticationPrincipal OAuth2User principal,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        User user = authHelper.getCurrentUser(principal);

        Page<TransactionResponse> transaction = transactionService.getAllWithFilter(user, filter, pageable);

        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta transações (que estão canceladas)")
    public ResponseEntity<Void> deleteTransaction(

            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        User user = authHelper.getCurrentUser(principal);

        transactionService.deleteTransaction(id, user.getId());

        return ResponseEntity.noContent().build();
    }
}
