package com.gustavosdaniel.myfinance_api.openApi;

import com.gustavosdaniel.myfinance_api.accounts.AccountRequest;
import com.gustavosdaniel.myfinance_api.accounts.AccountResponse;
import com.gustavosdaniel.myfinance_api.accounts.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.accounts.AccountUpdateRequest;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Accounts", description = "API responsável pelo gerenciamento de contas financeiras do usuário autenticado")
public interface AccountOpenApi {

    @Operation(
            summary = "Criar conta",
            description = "Cria uma nova conta financeira para o usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: Já existe uma conta com este nome para o usuário", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<AccountResponse> createdAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação da conta",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AccountRequest.class))
            )
            @Valid @RequestBody AccountRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Listar contas",
            description = "Retorna todas as contas financeiras do usuário autenticado. Pode ser filtrado pelo status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de contas retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseInfo.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetro de status inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<List<AccountResponseInfo>> getAllAccounts(

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(description = "Status da conta para filtro (ex: ACTIVE, INACTIVE)", example = "ACTIVE")
            @RequestParam(required = false) String status
    );

    @Operation(
            summary = "Buscar conta por nome",
            description = "Busca contas do usuário autenticado pelo nome informado ou parte dele."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas encontradas com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseInfo.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetro de nome ausente ou em branco", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<List<AccountResponseInfo>> searchByName(

            @Parameter(description = "Nome ou parte do nome da conta para busca", example = "Conta Corrente", required = true)
            @RequestParam
            @NotBlank String name,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Buscar conta por ID",
            description = "Retorna os detalhes de uma conta específica do usuário autenticado através do seu ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseInfo.class))),
            @ApiResponse(responseCode = "400", description = "Formato de ID inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou não pertence ao usuário", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<AccountResponseInfo> getAccountById(

            @Parameter(description = "ID único (UUID) da conta", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Atualizar conta",
            description = "Atualiza parcialmente os dados de uma conta financeira do usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseInfo.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou não pertence ao usuário", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: O novo nome já está em uso em outra conta", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<AccountResponseInfo> updateAccount(

            @Parameter(description = "ID único (UUID) da conta a ser atualizada", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para atualização da conta",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AccountUpdateRequest.class))
            )
            @Valid @RequestBody AccountUpdateRequest request
    );

    @Operation(
            summary = "Ativar conta",
            description = "Altera o status de uma conta inativa para ativa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta ativada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Formato de ID inválido ou conta já está ativa", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou não pertence ao usuário", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<Void> activateAccount(

            @Parameter(description = "ID único (UUID) da conta", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Desativar conta",
            description = "Altera o status de uma conta ativa para inativa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta desativada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Formato de ID inválido ou conta já está inativa", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou não pertence ao usuário", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<Void> deactivateAccount(

            @Parameter(description = "ID único (UUID) da conta", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Excluir conta",
            description = "Remove permanentemente uma conta financeira do usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta excluída com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Formato de ID inválido ou erro de restrição (ex: transações vinculadas)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou não pertence ao usuário", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: A conta não pode ser excluída", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<Void> deleteAccount(

            @Parameter(description = "ID único (UUID) da conta a ser excluída", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );
}
