package com.gustavosdaniel.myfinance_api.controller.openapi;

import com.gustavosdaniel.myfinance_api.domain.dto.AccountRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.UUID;

@Tag(name = "Contas", description = "API responsável pelo gerenciamento de contas financeiras do usuário")
public interface AccountOpenApi {

    /**
     * Cria uma nova conta associada ao usuário autenticado.
     *
     * @param request dados necessários para criação da conta
     * @param principal usuário autenticado no sistema
     * @return dados da conta criada
     */
    @Operation(
            summary = "Criar conta",
            description = "Cria uma nova conta financeira vinculada ao usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<AccountResponse> createdAccount(
            @RequestBody(description = "Dados para criação da conta", required = true,
                    content = @Content(schema = @Schema(implementation = AccountRequest.class)))
            AccountRequest request,

            @Parameter(hidden = true)
            OAuth2User principal
    );

    /**
     * Lista todas as contas do usuário autenticado.
     * Pode ser filtrado por status.
     *
     * @param principal usuário autenticado
     * @param status status da conta (opcional)
     * @return lista de contas
     */
    @Operation(
            summary = "Listar contas do usuário",
            description = "Retorna todas as contas associadas ao usuário autenticado. " +
                    "É possível filtrar pelo status da conta."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de contas retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<List<AccountResponseInfo>> getAllAccounts(

            @Parameter(hidden = true)
            OAuth2User principal,

            @Parameter(description = "Status da conta (ATIVA, INATIVA)", example = "ATIVA")
            String status
    );

    /**
     * Busca contas pelo nome da conta.
     *
     * @param name nome da conta
     * @param principal usuário autenticado
     * @return lista de contas que possuem o nome informado
     */
    @Operation(
            summary = "Buscar conta pelo nome",
            description = "Realiza a busca de contas pelo nome informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas encontradas"),
            @ApiResponse(responseCode = "404", description = "Nenhuma conta encontrada")
    })
    ResponseEntity<List<AccountResponseInfo>> searchByName(

            @Parameter(description = "Nome da conta", required = true, example = "Conta Corrente")
            String name,

            @Parameter(hidden = true)
            OAuth2User principal
    );

    /**
     * Busca uma conta específica pelo ID.
     *
     * @param id identificador da conta
     * @param principal usuário autenticado
     * @return informações da conta
     */
    @Operation(
            summary = "Buscar conta por ID",
            description = "Retorna os dados de uma conta específica a partir do seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada",
                    content = @Content(schema = @Schema(implementation = AccountResponseInfo.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    ResponseEntity<AccountResponseInfo> getAccountById(

            @Parameter(description = "ID da conta", required = true)
            UUID id,

            @Parameter(hidden = true)
            OAuth2User principal
    );

    /**
     * Atualiza as informações de uma conta existente.
     *
     * @param id identificador da conta
     * @param principal usuário autenticado
     * @param request novos dados da conta
     * @return conta atualizada
     */
    @Operation(
            summary = "Atualizar conta",
            description = "Atualiza as informações de uma conta existente do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    ResponseEntity<AccountResponseInfo> updateAccount(

            @Parameter(description = "ID da conta", required = true)
            UUID id,

            @Parameter(hidden = true)
            OAuth2User principal,

            @RequestBody(description = "Dados para atualização da conta",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AccountUpdateRequest.class)))
            AccountUpdateRequest request
    );

    /**
     * Ativa uma conta que esteja desativada.
     *
     * @param id identificador da conta
     * @param principal usuário autenticado
     * @return resposta sem conteúdo
     */
    @Operation(
            summary = "Ativar conta",
            description = "Ativa uma conta que esteja atualmente desativada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta ativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    ResponseEntity<Void> activateAccount(

            @Parameter(description = "ID da conta", required = true)
            UUID id,

            @Parameter(hidden = true)
            OAuth2User principal
    );

    /**
     * Desativa uma conta que esteja ativa.
     *
     * @param id identificador da conta
     * @param principal usuário autenticado
     * @return resposta sem conteúdo
     */
    @Operation(
            summary = "Desativar conta",
            description = "Desativa uma conta que esteja atualmente ativa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    ResponseEntity<Void> deactivateAccount(

            @Parameter(description = "ID da conta", required = true)
            UUID id,

            @Parameter(hidden = true)
            OAuth2User principal
    );

    /**
     * Remove permanentemente uma conta do usuário.
     *
     * @param id identificador da conta
     * @param principal usuário autenticado
     * @return resposta sem conteúdo
     */
    @Operation(
            summary = "Excluir conta",
            description = "Remove permanentemente uma conta do usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    ResponseEntity<Void> deleteAccount(

            @Parameter(description = "ID da conta", required = true)
            UUID id,

            @Parameter(hidden = true)
            OAuth2User principal
    );
}