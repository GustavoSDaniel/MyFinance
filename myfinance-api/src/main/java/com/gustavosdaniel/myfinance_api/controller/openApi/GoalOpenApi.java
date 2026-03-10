package com.gustavosdaniel.myfinance_api.controller.openApi;

import com.gustavosdaniel.myfinance_api.domain.dto.GoalRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalTransfer;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Goals",
        description = "API responsável pelo gerenciamento de metas financeiras do usuário"
)
public interface GoalOpenApi {

    @Operation(
            summary = "Criar meta",
            description = "Cria uma nova meta financeira para o usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Meta criada com sucesso",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<GoalResponse> create(

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação da meta",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GoalRequest.class))
            )
            @RequestBody @Valid GoalRequest request
    );


    @Operation(
            summary = "Buscar meta por ID",
            description = "Retorna uma meta específica do usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meta encontrada",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<GoalResponse> getById(

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @Parameter(description = "ID da meta", required = true)
            @PathVariable UUID id
    );


    @Operation(
            summary = "Buscar metas por nome",
            description = "Busca metas financeiras do usuário autenticado pelo nome."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metas encontradas",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<List<GoalResponse>> searchName(

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @Parameter(description = "Nome da meta", example = "Viagem", required = true)
            @RequestParam String name
    );


    @Operation(
            summary = "Listar metas",
            description = "Retorna uma lista paginada das metas financeiras do usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de metas retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Page<GoalResponse>> getAll(

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,

            @Parameter(description = "Status da meta", example = "ACTIVE")
            @RequestParam(required = false) String status
    );


    @Operation(
            summary = "Atualizar meta",
            description = "Atualiza os dados de uma meta financeira."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meta atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<GoalResponse> update(

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para atualização da meta",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GoalRequestUpdate.class))
            )
            @RequestBody @Valid GoalRequestUpdate requestUpdate,

            @Parameter(description = "ID da meta", required = true)
            @PathVariable UUID id
    );


    @Operation(
            summary = "Depositar valor na meta",
            description = "Adiciona um valor ao saldo atual da meta financeira."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Valor inválido"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<GoalResponse> deposit(

            @Parameter(description = "ID da meta", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Valor para depósito na meta",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GoalTransfer.class))
            )
            @RequestBody @Valid GoalTransfer transfer
    );


    @Operation(
            summary = "Sacar valor da meta",
            description = "Remove um valor do saldo atual da meta financeira."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saque realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Valor inválido"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<GoalResponse> withdraw(

            @Parameter(description = "ID da meta", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Valor para saque da meta",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GoalTransfer.class))
            )
            @RequestBody @Valid GoalTransfer transfer
    );


    @Operation(
            summary = "Excluir meta",
            description = "Remove permanentemente uma meta financeira do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Meta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> delete(

            @Parameter(description = "ID da meta", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );
}
