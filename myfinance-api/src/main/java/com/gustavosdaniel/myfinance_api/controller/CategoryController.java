package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.service.CategoryService;
import com.gustavosdaniel.myfinance_api.controller.openApi.CategoryOpenApi;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryResponseUpdate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController implements CategoryOpenApi{

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody @Valid CategoryRequest request,
            @AuthenticationPrincipal OAuth2User principal){

        return categoryService.createCategory(principal, request);

    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(required = false) String status)
    {

        return categoryService.getAllCategories(principal, status);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchName(

            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam String name
    ){
        return categoryService.searchByName(principal, name);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
            )
    {
        return categoryService.getById(id, principal);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponseUpdate> updateCategory(
            @PathVariable UUID id,
            @RequestBody @Valid CategoryRequestUpdate request,
            @AuthenticationPrincipal OAuth2User principal){

        return categoryService.updateCategory(id, principal, request)
;    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){

        return categoryService.activateCategory(id, principal);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ){
        return categoryService.deactivateCategory(id, principal);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(

            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable UUID id
    )
    {

        return categoryService.deleteCategory(id, principal);
    }
}
