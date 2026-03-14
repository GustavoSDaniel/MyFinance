package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.exception.CategoryNameDuplicateException;
import com.gustavosdaniel.myfinance_api.openApi.CategoryOpenApi;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController implements CategoryOpenApi {

    private final CategoryService categoryService;
    private final AuthHelper authHelper;

    public CategoryController(CategoryService categoryService, AuthHelper authHelper) {
        this.categoryService = categoryService;
        this.authHelper = authHelper;
    }

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

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status)
    {

        User user = authHelper.getCurrentUser(jwt);

        List<CategoryResponse> categories = categoryService.getAllCategories(user.getId(), status);

        return ResponseEntity.ok(categories);

    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchName(

            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String name
    ){
        User user = authHelper.getCurrentUser(jwt);

        List<CategoryResponse> categories = categoryService.searchByName(user.getId(), name);

        return ResponseEntity.ok(categories);

    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
            )
    {
        User user = authHelper.getCurrentUser(jwt);

        CategoryResponse category = categoryService.getById(id, user.getId());

        return ResponseEntity.ok(category);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponseUpdate> updateCategory(
            @PathVariable UUID id,
            @RequestBody @Valid CategoryRequestUpdate request,
            @AuthenticationPrincipal Jwt jwt){

        User user = authHelper.getCurrentUser(jwt);

        CategoryResponseUpdate category = categoryService.updateCategory(id, user.getId(), request);

        return ResponseEntity.ok(category);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        User user = authHelper.getCurrentUser(jwt);


        categoryService.activateCategory(id, user.getId());


        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){

        User user = authHelper.getCurrentUser(jwt);


        categoryService.deactivateCategory(id, user.getId());


        return ResponseEntity.noContent().build();
    }


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
