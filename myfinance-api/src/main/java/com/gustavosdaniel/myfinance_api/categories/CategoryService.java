package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(User user, CategoryRequest request) throws CategoryNameDuplicateException;

    List<CategoryResponse> getAllCategories(UUID userId, String status);

   CategoryResponse getById(UUID id, UUID userId);

   void deactivateCategory(UUID id, UUID userId);

   void  activateCategory(UUID id, UUID userId);

}
