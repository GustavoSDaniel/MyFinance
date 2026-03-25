package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.CategoryRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.CategoryResponseUpdate;
import com.gustavosdaniel.myfinance_api.controller.metrics.CategoryMetrics;
import com.gustavosdaniel.myfinance_api.controller.openApi.CategoryOpenApi;
import com.gustavosdaniel.myfinance_api.service.CategoryService;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST responsável por gerenciar as requisições relacionadas às categorias financeiras (Categories) dos usuários.
 *
 * <p>Fornece endpoints para criação, listagem, busca, atualização, ativação, desativação
 * e remoção de categorias vinculadas ao usuário autenticado.</p>
 *
 * <p>Os DTOs utilizados são:
 * <ul>
 *   <li>{@link CategoryRequest} – entrada para criação de categoria</li>
 *   <li>{@link CategoryRequestUpdate} – entrada para atualização de categoria</li>
 *   <li>{@link CategoryResponse} – saída para listagens e busca por ID</li>
 *   <li>{@link CategoryResponseUpdate} – saída detalhada após atualização</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController implements CategoryOpenApi {

    private final CategoryService categoryService;
    private final AuthHelper authHelper;
    private final CategoryMetrics categoryMetrics;

    public CategoryController(CategoryService categoryService, AuthHelper authHelper, CategoryMetrics categoryMetrics) {
        this.categoryService = categoryService;
        this.authHelper = authHelper;
        this.categoryMetrics = categoryMetrics;
    }

    /**
     * Cria uma nova categoria vinculada ao usuário atualmente autenticado.
     * <p>
     * Em caso de sucesso, o cabeçalho {@code Location} conterá a URI da nova categoria.
     * </p>
     *
     * @param request os dados necessários para a criação da categoria
     * @param jwt     o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} com status 201 (Created), a URI da nova categoria no cabeçalho Location
     *         e os dados criados no corpo
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody @Valid CategoryRequest request,
            @AuthenticationPrincipal Jwt jwt){

        User user = authHelper.getCurrentUser(jwt);

        CategoryResponse category = categoryService.createCategory(user,request);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(category.id())
                .toUri();

        return ResponseEntity.created(uri).body(category);

    }

    /**
     * Retorna uma lista com todas as categorias do usuário autenticado, com a opção de filtrar pelo status.
     * <p>
     * O parâmetro {@code status} é tratado de forma case‑insensitive e aceita os valores:
     * "active", "disabled" ou qualquer outro valor para listar todas as categorias.
     * </p>
     *
     * @param jwt    o token JWT contendo as credenciais do usuário
     * @param status filtro opcional pelo status da categoria (ex: "active", "disabled")
     * @return um {@link ResponseEntity} contendo a lista de {@link CategoryResponse} com as categorias encontradas
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status)
    {

        User user = authHelper.getCurrentUser(jwt);

        List<CategoryResponse> categories = categoryService.getAllCategories(user.getId(), status);

        return categoryMetrics.recordGetAll(() -> ResponseEntity.ok(categories));

    }

    /**
     * Realiza uma busca por categorias do usuário autenticado cujo nome corresponda ao termo informado.
     * <p>
     * A busca é case‑insensitive e retorna categorias cujo nome contenha o termo informado.
     * </p>
     *
     * @param jwt  o token JWT contendo as credenciais do usuário
     * @param name o termo ou nome a ser pesquisado (busca parcial)
     * @return um {@link ResponseEntity} contendo a lista de categorias que correspondem à busca
     */
    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchName(

            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String name
    ){
        User user = authHelper.getCurrentUser(jwt);

        List<CategoryResponse> categories = categoryService.searchByName(user.getId(), name);

        return categoryMetrics.recordSearchName(() -> ResponseEntity.ok(categories));

    }

    /**
     * Busca os detalhes de uma categoria específica pertencente ao usuário autenticado.
     *
     * @param id  o identificador único (UUID) da categoria a ser buscada
     * @param jwt o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} contendo as informações da categoria
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
            )
    {
        User user = authHelper.getCurrentUser(jwt);

        CategoryResponse category = categoryService.getById(id, user.getId());

        return categoryMetrics.recordGetById(() -> ResponseEntity.ok(category));
    }

    /**
     * Atualiza os dados de uma categoria existente pertencente ao usuário autenticado.
     * <p>
     * Apenas os campos enviados no corpo da requisição serão atualizados.
     * </p>
     *
     * @param id      o identificador único (UUID) da categoria a ser atualizada
     * @param request os novos dados a serem aplicados na categoria
     * @param jwt     o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} contendo as informações atualizadas da categoria
     */
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponseUpdate> updateCategory(
            @PathVariable UUID id,
            @RequestBody @Valid CategoryRequestUpdate request,
            @AuthenticationPrincipal Jwt jwt){

        User user = authHelper.getCurrentUser(jwt);

        CategoryResponseUpdate category = categoryService.updateCategory(id, user.getId(), request);

        return ResponseEntity.ok(category);
    }

    /**
     * Ativa uma categoria previamente inativada pertencente ao usuário autenticado.
     * <p>
     * Se a categoria já estiver ativa, a operação é ignorada (idempotente).
     * </p>
     *
     * @param id  o identificador único (UUID) da categoria a ser ativada
     * @param jwt o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} com status 204 (No Content) indicando sucesso na operação
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        User user = authHelper.getCurrentUser(jwt);


        categoryService.activateCategory(id, user.getId());


        return ResponseEntity.noContent().build();
    }

    /**
     * Desativa uma categoria pertencente ao usuário autenticado.
     * <p>
     * Se a categoria já estiver inativa, a operação é ignorada (idempotente).
     * </p>
     *
     * @param id  o identificador único (UUID) da categoria a ser desativada
     * @param jwt o token JWT contendo as credenciais do usuário
     * @return um {@link ResponseEntity} com status 204 (No Content) indicando sucesso na operação
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        User user = authHelper.getCurrentUser(jwt);


        categoryService.deactivateCategory(id, user.getId());


        return ResponseEntity.noContent().build();
    }

    /**
     * Remove de forma permanente uma categoria pertencente ao usuário autenticado.
     * <p>
     * A exclusão só é permitida se a categoria não possuir transações ou metas vinculadas.
     * </p>
     *
     * @param jwt o token JWT contendo as credenciais do usuário
     * @param id  o identificador único (UUID) da categoria a ser removida
     * @return um {@link ResponseEntity} com status 204 (No Content) indicando sucesso na deleção
     * @throws IllegalArgumentException    se a categoria ainda possuir vínculos (transações/metas)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(

            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    )
    {
        User user = authHelper.getCurrentUser(jwt);

        categoryService.deleteCategory(id, user);

        return ResponseEntity.noContent().build();
    }
}
