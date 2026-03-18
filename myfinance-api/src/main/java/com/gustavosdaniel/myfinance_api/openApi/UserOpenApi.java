package com.gustavosdaniel.myfinance_api.openApi;

import com.gustavosdaniel.myfinance_api.user.UserInfoResponse;
import com.gustavosdaniel.myfinance_api.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;


import java.util.UUID;

@Tag(name = "Users", description = "API responsável pelo gerenciamento de usuários do sistema")
public interface UserOpenApi {

    @Operation(
            summary = "Obter dados do usuário logado",
            description = "Retorna as informações resumidas (nome, email, foto) do usuário atualmente autenticado no sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    UserInfoResponse getCurrentUser();


    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna uma lista paginada de todos os usuários cadastrados no sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários retornados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado (permissão insuficiente)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    );


    @Operation(
            summary = "Buscar usuário por email",
            description = "Retorna os dados de um usuário com base no email informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de email inválido ou parâmetro ausente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado (permissão insuficiente)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado com o email informado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<UserResponse> getEmailByUser(
            @Parameter(description = "Email do usuário a ser buscado", example = "usuario@email.com", required = true)
            @RequestParam String email
    );


    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna os dados de um usuário a partir do seu identificador único (UUID)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de ID (UUID) inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado (permissão insuficiente)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado com o ID informado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID único (UUID) do usuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id
    );


    @Operation(
            summary = "Deletar usuário",
            description = "Remove um usuário do sistema. Administradores (ROLE_ADMIN) podem remover qualquer usuário. Usuários comuns só podem remover a própria conta."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Formato de ID (UUID) inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não possui permissão para deletar esta conta)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado com o ID informado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID único (UUID) do usuário que será removido", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            Authentication authentication
    );
}