package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.controller.metrics.CategoryMetrics;
import com.gustavosdaniel.myfinance_api.service.CategoryService;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryResponseUpdate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController{

    private final CategoryService categoryService;
    private final CategoryMetrics categoryMetrics;

    public CategoryController(CategoryService categoryService, CategoryMetrics categoryMetrics) {
        this.categoryService = categoryService;
        this.categoryMetrics = categoryMetrics;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody @Valid CategoryRequest request,
            @AuthenticationPrincipal Jwt jwt){

        return categoryService.createCategory(jwt, request);

    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status)
    {

        return categoryMetrics.recordGetAll(() -> categoryService.getAllCategories(jwt, status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchName(

            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String name
    ){
        return categoryMetrics.recordSearchName(() -> categoryService.searchByName(jwt, name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
            )
    {
        return categoryMetrics.recordGetById(() -> categoryService.getById(id, jwt));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponseUpdate> updateCategory(
            @PathVariable UUID id,
            @RequestBody @Valid CategoryRequestUpdate request,
            @AuthenticationPrincipal Jwt jwt){

        return categoryService.updateCategory(id, jwt, request)
;    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        return categoryService.activateCategory(id, jwt);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){
        return categoryService.deactivateCategory(id, jwt);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(

            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    )
    {

        return categoryService.deleteCategory(id, jwt);
    }
}
