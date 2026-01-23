package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.user.User;

public interface CategoryService {

    CategoryResponse createCategory(User user, CategoryRequest request) throws CategoryNameDuplicateException;
}
