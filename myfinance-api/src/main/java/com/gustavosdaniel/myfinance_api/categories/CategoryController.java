package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthHelper authHelper;


    public CategoryController(CategoryService categoryService, AuthHelper authHelper) {
        this.categoryService = categoryService;
        this.authHelper = authHelper;
    }

    @PostMapping
    @Operation(summary = "Cria categoria para usuário")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody @Valid CategoryRequest request,
            @AuthenticationPrincipal OAuth2User principal) throws CategoryNameDuplicateException {

        User user = authHelper.getCurrentUser(principal);

        CategoryResponse category = categoryService.createCategory(user,request);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(category.id())
                .toUri();

        return ResponseEntity.created(uri).body(category);

    }

    @GetMapping
    @Operation(summary = "Mostras a categorias do usuário")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(required = false) String status)
    {

        User user = authHelper.getCurrentUser(principal);

        List<CategoryResponse> categories = categoryService.getAllCategories(user.getId(), status);

        return ResponseEntity.ok(categories);

    }

    @GetMapping("/search")
    @Operation(summary = "Busca categoria do usuário pelo nome")
    public ResponseEntity<List<CategoryResponse>> searchName(

            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam String name
    ){
        User user = authHelper.getCurrentUser(principal);

        List<CategoryResponse> categories = categoryService.searchByName(user.getId(), name);

        return ResponseEntity.ok(categories);

    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca categoria do usuário pelo ID")
    public ResponseEntity<CategoryResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
            )
    {
        User user = authHelper.getCurrentUser(principal);

        CategoryResponse category = categoryService.getById(id, user.getId());

        return ResponseEntity.ok(category);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualiza a categoria do usuário")
    public ResponseEntity<CategoryResponseUpdate> updateCategory(
            @PathVariable UUID id,
            @RequestBody @Valid CategoryRequestUpdate request,
            @AuthenticationPrincipal OAuth2User principal) throws CategoryNameDuplicateException {

        User user = authHelper.getCurrentUser(principal);

        CategoryResponseUpdate category = categoryService.updateCategory(id, user.getId(), request);

        return ResponseEntity.ok(category);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativa categoria do usuário")
    public ResponseEntity<Void> activateCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        User user = authHelper.getCurrentUser(principal);


        categoryService.activateCategory(id, user.getId());


        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativa categoria do usuário")
    public ResponseEntity<Void> deactivateCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        User user = authHelper.getCurrentUser(principal);


        categoryService.deactivateCategory(id, user.getId());


        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta categoria do usuário")
    public ResponseEntity<Void> deleteCategory(

            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable UUID id
    )
    {
        User user = authHelper.getCurrentUser(principal);

        categoryService.deleteCategory(id, user);

        return ResponseEntity.noContent().build();
    }
}
