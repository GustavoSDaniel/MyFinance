package com.gustavosdaniel.myfinance_api.controller.openApi;

import com.gustavosdaniel.myfinance_api.domain.dto.response.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.dto.request.AccountUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Accounts",
        description = "API responsável pelo gerenciamento de contas financeiras do usuário autenticado"
)
public interface AccountOpenApi {

    @Operation(
            summary = "Listar contas",
            tags = {"Categories"},
            description = "Retorna todas as contas financeiras do usuário autenticado. Pode ser filtrado pelo status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de contas retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = AccountResponseInfo.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<List<AccountResponseInfo>> getAllAccounts(

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @Parameter(description = "Status da conta para filtro", example = "ACTIVE")
            @RequestParam(required = false) String status
    );


    @Operation(
            summary = "Buscar conta por nome",
            description = "Busca contas do usuário autenticado pelo nome informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas encontradas",
                    content = @Content(schema = @Schema(implementation = AccountResponseInfo.class))),
            @ApiResponse(responseCode = "400", description = "Nome inválido"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<List<AccountResponseInfo>> searchByName(

            @Parameter(description = "Nome da conta para busca", example = "Conta Corrente", required = true)
            @RequestParam
            @NotBlank String name,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Buscar conta por ID",
            description = "Retorna uma conta específica do usuário autenticado através do ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada",
                    content = @Content(schema = @Schema(implementation = AccountResponseInfo.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<AccountResponseInfo> getAccountById(

            @Parameter(description = "ID da conta", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Atualizar conta",
            description = "Atualiza os dados de uma conta financeira do usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = AccountResponseInfo.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<AccountResponseInfo> updateAccount(

            @Parameter(description = "ID da conta", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para atualização da conta",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AccountUpdateRequest.class))
            )
            @Valid @RequestBody AccountUpdateRequest request
    );


    @Operation(
            summary = "Ativar conta",
            description = "Ativa uma conta financeira do usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta ativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> activateAccount(

            @Parameter(description = "ID da conta", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Desativar conta",
            description = "Desativa uma conta financeira do usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> deactivateAccount(

            @Parameter(description = "ID da conta", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Excluir conta",
            description = "Remove permanentemente uma conta financeira do usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> deleteAccount(

            @Parameter(description = "ID da conta", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );
}
