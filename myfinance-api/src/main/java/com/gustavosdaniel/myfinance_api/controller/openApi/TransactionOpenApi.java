package com.gustavosdaniel.myfinance_api.controller.openApi;

import com.gustavosdaniel.myfinance_api.domain.dto.request.TransactionRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.response.TransactionResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.TransactionSearchFilter;
import com.gustavosdaniel.myfinance_api.domain.dto.request.TransferRequest;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(
        name = "Transactions",
        description = "API responsável pelo gerenciamento de transações financeiras do usuário"
)
public interface TransactionOpenApi {

    @Operation(
            summary = "Criar transação",
            description = "Cria uma nova transação financeira (receita ou despesa) vinculada ao usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transação criada com sucesso",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<TransactionResponse> createTransaction(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da transação",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransactionRequest.class))
            )
            @RequestBody @Valid TransactionRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Confirmar transação",
            description = "Confirma uma transação pendente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transação confirmada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> confirmTransaction(

            @Parameter(description = "ID da transação", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Cancelar transação",
            description = "Cancela uma transação existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transação cancelada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> cancelTransaction(

            @Parameter(description = "ID da transação", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Transferir valor entre contas",
            description = "Realiza uma transferência de valores entre contas do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transferência realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> transfer(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da transferência",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransferRequest.class))
            )
            @RequestBody @Valid TransferRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Buscar transação por ID",
            description = "Retorna os detalhes de uma transação específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transação encontrada",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<TransactionResponse> findById(

            @Parameter(description = "ID da transação", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Listar transações com filtro",
            description = "Retorna uma lista paginada de transações do usuário com possibilidade de filtros."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de transações retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Page<TransactionResponse>> allTransactionsWithFilter(

            @ParameterObject
            TransactionSearchFilter filter,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    );


    @Operation(
            summary = "Excluir transação",
            description = "Remove permanentemente uma transação financeira."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transação excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> deleteTransaction(

            @Parameter(description = "ID da transação", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );
}
