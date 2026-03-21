package com.gustavosdaniel.myfinance_api.controller.openApi;

import com.gustavosdaniel.myfinance_api.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.CategoryRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.CategoryResponseUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Categories", description = "API responsável pelo gerenciamento de categorias financeiras do usuário autenticado")
public interface CategoryOpenApi {

    @Operation(
            summary = "Criar categoria",
            description = "Cria uma nova categoria financeira (como 'Alimentação' ou 'Salário') vinculada ao usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: Já existe uma categoria com este nome para o mesmo tipo", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<CategoryResponse> createCategory(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação da categoria",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoryRequest.class))
            )
            @RequestBody @Valid CategoryRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Listar categorias",
            description = "Retorna todas as categorias financeiras do usuário autenticado. Pode ser filtrado pelo status (ex: ativas ou inativas)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetro de status inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<List<CategoryResponse>> getAllCategories(

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(description = "Status da categoria para filtro (ex: ACTIVE, INACTIVE)", example = "ACTIVE")
            @RequestParam(required = false) String status
    );

    @Operation(
            summary = "Buscar categoria por nome",
            description = "Busca categorias do usuário autenticado pelo nome informado ou parte dele."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorias encontradas com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetro de nome ausente ou em branco", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<List<CategoryResponse>> searchName(

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(description = "Nome ou parte do nome da categoria para busca", example = "Alimentação", required = true)
            @RequestParam String name
    );

    @Operation(
            summary = "Buscar categoria por ID",
            description = "Retorna os detalhes de uma categoria específica do usuário autenticado através do seu ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de ID inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou não pertence ao usuário", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<CategoryResponse> getById(

            @Parameter(description = "ID único (UUID) da categoria", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Atualizar categoria",
            description = "Atualiza parcialmente os dados de uma categoria financeira existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseUpdate.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou não pertence ao usuário", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: O novo nome já está em uso em outra categoria do mesmo tipo", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<CategoryResponseUpdate> updateCategory(

            @Parameter(description = "ID único (UUID) da categoria a ser atualizada", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para atualização da categoria",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoryRequestUpdate.class))
            )
            @RequestBody @Valid CategoryRequestUpdate request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Ativar categoria",
            description = "Altera o status de uma categoria inativa para ativa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria ativada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Formato de ID inválido ou categoria já está ativa", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou não pertence ao usuário", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<Void> activateCategory(

            @Parameter(description = "ID único (UUID) da categoria", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Desativar categoria",
            description = "Altera o status de uma categoria ativa para inativa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria desativada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Formato de ID inválido ou categoria já está inativa", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou não pertence ao usuário", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<Void> deactivateCategory(

            @Parameter(description = "ID único (UUID) da categoria", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Excluir categoria",
            description = "Remove permanentemente uma categoria financeira do usuário. Só pode ser excluída se não possuir transações vinculadas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria excluída com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Formato de ID inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou não pertence ao usuário", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: A categoria não pode ser excluída pois possui transações vinculadas", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<Void> deleteCategory(

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(description = "ID único (UUID) da categoria a ser excluída", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable UUID id
    );
}