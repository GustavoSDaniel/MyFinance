package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.exception.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.metrics.TransactionMetrics;
import com.gustavosdaniel.myfinance_api.openApi.TransactionOpenApi;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Controlador REST responsável por expor os endpoints da API para gerenciamento de transações.
 * <p>
 * Esta classe implementa a interface {@link TransactionOpenApi} (para documentação OpenAPI) e
 * fornece operações de criação, consulta, atualização de status (confirmação/cancelamento),
 * transferência entre contas, busca com filtros e exclusão de transações.
 * </p>
 * <p>
 * Todos os endpoints exigem autenticação via JWT, e o usuário autenticado é obtido através
 * de {@link AuthHelper} a partir do token JWT fornecido no cabeçalho de autorização.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController implements TransactionOpenApi {

    private final TransactionService transactionService;
    private final AuthHelper authHelper;
    private final TransactionMetrics transactionMetrics;

    public TransactionController(TransactionService transactionService, AuthHelper authHelper, TransactionMetrics transactionMetrics) {
        this.transactionService = transactionService;
        this.authHelper = authHelper;
        this.transactionMetrics = transactionMetrics;
    }


    /**
     * Cria uma nova transação (receita ou despesa) para o usuário autenticado.
     * <p>
     * O corpo da requisição deve conter os dados da transação no formato {@link TransactionRequest}.
     * Após a criação, retorna o status {@code 201 Created} com a localização do novo recurso
     * no cabeçalho {@code Location} e o corpo contendo os detalhes da transação criada.
     * </p>
     *
     * @param request Objeto contendo os dados da transação a ser criada (validado via {@link Valid}).
     * @param jwt     Token JWT do usuário autenticado (extraído pelo Spring Security).
     * @return {@link ResponseEntity} com status 201, cabeçalho Location e corpo {@link TransactionResponse}.
     * @throws InvalidAmountException          Se o valor da transação for inválido (ex.: negativo ou zero).
     * @throws InsufficientBalanceException    Se for uma despesa e o saldo da conta for insuficiente.
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody @Valid TransactionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) throws InvalidAmountException, InsufficientBalanceException {

        User user = authHelper.getCurrentUser(jwt);

        TransactionResponse transaction = transactionService.createTransaction(user, request);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transaction.id())
                .toUri();

        return ResponseEntity.created(uri).body(transaction);
    }

    /**
     * Confirma uma transação pendente, alterando seu status para CONFIRMADA.
     * <p>
     * A confirmação de uma transação pode impactar o saldo da conta associada
     * (dependendo da regra de negócio, por exemplo, em despesas futuras).
     * </p>
     *
     * @param id  Identificador único da transação a ser confirmada.
     * @param jwt Token JWT do usuário autenticado.
     * @return {@link ResponseEntity} com status 204 No Content em caso de sucesso.
     * @throws InvalidAmountException       Se houver problema com o valor da transação durante a confirmação.
     * @throws InsufficientBalanceException Se a confirmação de uma despesa resultar em saldo insuficiente.
     */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Void> confirmTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) throws InvalidAmountException, InsufficientBalanceException {

        User user = authHelper.getCurrentUser(jwt);

        transactionService.transactionConfirmed(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Cancela uma transação pendente, alterando seu status para CANCELADA.
     * <p>
     * O cancelamento impede que a transação afete o saldo da conta (se ainda não processada).
     * </p>
     *
     * @param id  Identificador único da transação a ser cancelada.
     * @param jwt Token JWT do usuário autenticado.
     * @return {@link ResponseEntity} com status 204 No Content em caso de sucesso.
     * @throws InvalidAmountException       Se houver problema com o valor da transação durante o cancelamento.
     * @throws InsufficientBalanceException Se o cancelamento impactar o saldo de forma inconsistente.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) throws InvalidAmountException, InsufficientBalanceException {

        User user = authHelper.getCurrentUser(jwt);

        transactionService.transactionCancel(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Realiza uma transferência de valor entre duas contas do mesmo usuário.
     * <p>
     * O corpo da requisição deve conter os dados da transferência no formato {@link TransferRequest}.
     * A operação cria duas transações: uma despesa na conta de origem e uma receita na conta de destino.
     * </p>
     *
     * @param request Objeto contendo os dados da transferência (conta origem, conta destino, valor, etc.).
     * @param jwt     Token JWT do usuário autenticado.
     * @return {@link ResponseEntity} com status 204 No Content em caso de sucesso.
     */
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @RequestBody @Valid TransferRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){

        User user = authHelper.getCurrentUser(jwt);

        transactionService.transfer(user, request);

        return ResponseEntity.noContent().build();
    }

    /**
     * Busca uma transação pelo seu ID, desde que pertença ao usuário autenticado.
     *
     * @param id  Identificador único da transação.
     * @param jwt Token JWT do usuário autenticado.
     * @return {@link ResponseEntity} com status 200 e o corpo contendo os dados da transação
     *         em {@link TransactionResponse}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        User user = authHelper.getCurrentUser(jwt);

        TransactionResponse transaction = transactionService
                .getTransactionById(id, user.getId());

        return transactionMetrics.recordGetById(() -> ResponseEntity.ok(transaction));

    }

    /**
     * Retorna uma página de transações do usuário autenticado, com suporte a filtros e ordenação.
     * <p>
     * Os filtros são fornecidos através do objeto {@link TransactionSearchFilter} como parâmetros de consulta.
     * A paginação e ordenação são controladas pelos parâmetros {@code page}, {@code size}, {@code sort}
     * (padrão: ordenação decrescente por {@code createdAt}).
     * </p>
     *
     * @param filter   Objeto contendo os critérios de filtragem (conta, categoria, descrição, tipo, status, datas).
     * @param jwt      Token JWT do usuário autenticado.
     * @param pageable Configurações de paginação e ordenação.
     * @return {@link ResponseEntity} com status 200 e uma página de {@link TransactionResponse}.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponse>> allTransactionsWithFilter(

            @ParameterObject TransactionSearchFilter filter,
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        User user = authHelper.getCurrentUser(jwt);

        Page<TransactionResponse> transaction = transactionService
                .getAllWithFilter(user, filter, pageable);

        return transactionMetrics.recordGetAll(() -> ResponseEntity.ok(transaction));
    }

    /**
     * Exclui uma transação do usuário autenticado.
     * <p>
     * A exclusão pode estar sujeita a regras como não permitir exclusão de transações já confirmadas.
     * </p>
     *
     * @param id  Identificador único da transação a ser excluída.
     * @param jwt Token JWT do usuário autenticado.
     * @return {@link ResponseEntity} com status 204 No Content em caso de sucesso.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(

            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        User user = authHelper.getCurrentUser(jwt);

        transactionService.deleteTransaction(id, user.getId());

        return ResponseEntity.noContent().build();
    }
}
