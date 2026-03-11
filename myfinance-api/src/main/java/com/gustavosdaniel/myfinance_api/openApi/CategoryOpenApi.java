package com.gustavosdaniel.myfinance_api.openApi;

import com.gustavosdaniel.myfinance_api.categories.CategoryRequest;
import com.gustavosdaniel.myfinance_api.categories.CategoryRequestUpdate;
import com.gustavosdaniel.myfinance_api.categories.CategoryResponse;
import com.gustavosdaniel.myfinance_api.categories.CategoryResponseUpdate;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Categories",
        description = "API responsável pelo gerenciamento de categorias financeiras do usuário"
)
public interface CategoryOpenApi {

    @Operation(
            summary = "Criar categoria",
            description = "Cria uma nova categoria financeira vinculada ao usuário autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<CategoryResponse> createCategory(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação da categoria",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoryRequest.class))
            )
            @RequestBody @Valid CategoryRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Listar categorias",
            description = "Retorna todas as categorias do usuário autenticado. Pode ser filtrado pelo status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<List<CategoryResponse>> getAllCategories(

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @Parameter(description = "Status da categoria", example = "ACTIVE")
            @RequestParam(required = false) String status
    );


    @Operation(
            summary = "Buscar categoria por nome",
            description = "Busca categorias do usuário autenticado pelo nome."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorias encontradas",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nome inválido"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<List<CategoryResponse>> searchName(

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @Parameter(description = "Nome da categoria", example = "Alimentação", required = true)
            @RequestParam String name
    );


    @Operation(
            summary = "Buscar categoria por ID",
            description = "Retorna uma categoria específica do usuário autenticado pelo ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<CategoryResponse> getById(

            @Parameter(description = "ID da categoria", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Atualizar categoria",
            description = "Atualiza os dados de uma categoria financeira."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryResponseUpdate.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<CategoryResponseUpdate> updateCategory(

            @Parameter(description = "ID da categoria", required = true)
            @PathVariable UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para atualização da categoria",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoryRequestUpdate.class))
            )
            @RequestBody @Valid CategoryRequestUpdate request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Ativar categoria",
            description = "Ativa uma categoria financeira do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria ativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> activateCategory(

            @Parameter(description = "ID da categoria", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Desativar categoria",
            description = "Desativa uma categoria financeira do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> deactivateCategory(

            @Parameter(description = "ID da categoria", required = true)
            @PathVariable UUID id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    );


    @Operation(
            summary = "Excluir categoria",
            description = "Remove permanentemente uma categoria financeira do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> deleteCategory(

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @Parameter(description = "ID da categoria", required = true)
            @PathVariable UUID id
    );
}
