package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.user.User;

import java.util.Optional;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(User user, CategoryRequest request) throws CategoryNameDuplicateException;

   CategoryResponse getById(UUID id, UUID userId);
}
