package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.exception.CategoryNameDuplicateException;
import com.gustavosdaniel.myfinance_api.user.User;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(User user, CategoryRequest request) throws CategoryNameDuplicateException;

    List<CategoryResponse> getAllCategories(UUID userId, String status);

    List<CategoryResponse> searchByName(UUID userId,String name);

   CategoryResponse getById(UUID id, UUID userId);

    CategoryResponseUpdate updateCategory(UUID id, UUID userId, CategoryRequestUpdate request) throws CategoryNameDuplicateException;

   void deactivateCategory(UUID id, UUID userId);

   void  activateCategory(UUID id, UUID userId);

   void deleteCategory(UUID id , User user);

}
