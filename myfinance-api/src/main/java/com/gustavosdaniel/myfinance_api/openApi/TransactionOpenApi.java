package com.gustavosdaniel.myfinance_api.openApi;

import com.gustavosdaniel.myfinance_api.transactions.TransactionRequest;
import com.gustavosdaniel.myfinance_api.transactions.TransactionResponse;
import com.gustavosdaniel.myfinance_api.transactions.TransactionSearchFilter;
import com.gustavosdaniel.myfinance_api.transactions.TransferRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;


/**
 * Interface responsável por definir o contrato da API de transações financeiras,
 * sendo utilizada para documentação OpenAPI/Swagger.
 * <p>
 * Esta interface declara todos os endpoints relacionados ao gerenciamento de transações,
 * incluindo criação, consulta, confirmação, cancelamento, transferência entre contas,
 * busca com filtros e exclusão. As implementações concretas devem estar em
 * </p>
 *
 * @author Gustavo Daniel
 * @version 1.0
 */
@Tag(
        name = "Transactions",
        description = "API responsável pelo gerenciamento de transações financeiras do usuário"
)
public interface TransactionOpenApi {

    /**
     * Cria uma nova transação financeira (receita ou despesa) vinculada ao usuário autenticado.
     *
     * @param request Dados da transação a ser criada (validação via {@link Valid}).
     * @param jwt     Token JWT do usuário autenticado (ignorado na documentação).
     * @return {@link ResponseEntity} contendo os dados da transação criada e status 201.
     */
    @Operation(
            summary = "Criar transação",
            description = "Cria uma nova transação financeira (receita ou despesa) vinculada ao usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transação criada com sucesso",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex.: valor negativo, campos obrigatórios ausentes)"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário sem permissão para a conta ou categoria)"),
            @ApiResponse(responseCode = "422", description = "Erro de negócio: saldo insuficiente para despesa (InsufficientBalanceException) ou valor inválido (InvalidAmountException)"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<TransactionResponse> createTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da transação",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransactionRequest.class))
            )
            @RequestBody @Valid TransactionRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    /**
     * Confirma uma transação pendente, alterando seu status para CONFIRMADA.
     *
     * @param id  ID da transação a ser confirmada.
     * @param jwt Token JWT do usuário autenticado.
     * @return {@link ResponseEntity} com status 204 em caso de sucesso.
     */
    @Operation(
            summary = "Confirmar transação",
            description = "Confirma uma transação pendente, alterando seu status para CONFIRMADA e, se for o caso, efetivando o lançamento no saldo da conta."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transação confirmada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido ou transação não está pendente"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (transação pertence a outro usuário)"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
            @ApiResponse(responseCode = "422", description = "Erro de negócio: saldo insuficiente ao confirmar despesa (InsufficientBalanceException)"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> confirmTransaction(
            @Parameter(description = "ID da transação", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    /**
     * Cancela uma transação existente, alterando seu status para CANCELADA.
     *
     * @param id  ID da transação a ser cancelada.
     * @param jwt Token JWT do usuário autenticado.
     * @return {@link ResponseEntity} com status 204 em caso de sucesso.
     */
    @Operation(
            summary = "Cancelar transação",
            description = "Cancela uma transação existente, alterando seu status para CANCELADA (impedindo que afete o saldo)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transação cancelada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido ou transação não pode ser cancelada (já confirmada/cancelada)"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (transação pertence a outro usuário)"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> cancelTransaction(
            @Parameter(description = "ID da transação", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    /**
     * Realiza uma transferência de valores entre contas do usuário autenticado.
     *
     * @param request Dados da transferência (conta origem, conta destino, valor, etc.).
     * @param jwt     Token JWT do usuário autenticado.
     * @return {@link ResponseEntity} com status 204 em caso de sucesso.
     */
    @Operation(
            summary = "Transferir valor entre contas",
            description = "Realiza uma transferência de valores entre contas do usuário, criando uma despesa na origem e uma receita no destino."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transferência realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex.: contas iguais, valor negativo, campos obrigatórios ausentes)"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (contas não pertencem ao usuário)"),
            @ApiResponse(responseCode = "404", description = "Conta origem ou destino não encontrada"),
            @ApiResponse(responseCode = "422", description = "Erro de negócio: saldo insuficiente na conta origem"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> transfer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da transferência",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransferRequest.class))
            )
            @RequestBody @Valid TransferRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    /**
     * Busca uma transação específica pelo seu ID.
     *
     * @param id  ID da transação.
     * @param jwt Token JWT do usuário autenticado.
     * @return {@link ResponseEntity} contendo os dados da transação e status 200.
     */
    @Operation(
            summary = "Buscar transação por ID",
            description = "Retorna os detalhes de uma transação específica pertencente ao usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transação encontrada",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (transação pertence a outro usuário)"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<TransactionResponse> findById(
            @Parameter(description = "ID da transação", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    /**
     * Retorna uma lista paginada de transações do usuário autenticado, com suporte a filtros.
     *
     * @param filter   Critérios de filtragem (conta, categoria, descrição, tipo, status, período).
     * @param jwt      Token JWT do usuário autenticado.
     * @param pageable Parâmetros de paginação e ordenação (padrão: ordenação decrescente por createdAt).
     * @return Página de transações no formato {@link TransactionResponse}.
     */
    @Operation(
            summary = "Listar transações com filtro",
            description = "Retorna uma lista paginada de transações do usuário com possibilidade de filtros por conta, categoria, descrição, tipo, status e período."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de transações retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de filtro inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Page<TransactionResponse>> allTransactionsWithFilter(
            @ParameterObject TransactionSearchFilter filter,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    );

    /**
     * Exclui (remove) permanentemente uma transação financeira.
     *
     * @param id  ID da transação a ser excluída.
     * @param jwt Token JWT do usuário autenticado.
     * @return {@link ResponseEntity} com status 204 em caso de sucesso.
     */
    @Operation(
            summary = "Excluir transação",
            description = "Remove permanentemente uma transação financeira. A operação pode ser restrita a transações com status pendente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transação excluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido ou transação não pode ser excluída (ex.: já confirmada)"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (transação pertence a outro usuário)"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "ID da transação", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );
}