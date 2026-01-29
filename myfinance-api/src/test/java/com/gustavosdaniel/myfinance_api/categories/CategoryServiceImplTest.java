package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Nested
    class createCategory{

        @Test
        @DisplayName("Should created category with sucesso")
        void shouldCreateCategory() throws CategoryNameDuplicateException {

            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            CategoryRequest request = new CategoryRequest("Lazer", CategoryType.DESPESA, "#008000");

            Category category = new Category(user, "Lazer", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            CategoryResponse response =
                    new CategoryResponse(categoryId,"Lazer", CategoryType.DESPESA, "#008000");


            when(categoryRepository.existsByNameIgnoreCaseAndUserIdAndType(
                    category.getName().trim(), userId, category.getType())).thenReturn(false);
            when(categoryMapper.toCategory(request)).thenReturn(category);
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toCategoryResponse(category)).thenReturn(response);

            CategoryResponse output = categoryService.createCategory(user, request);

            assertNotNull(output);
            assertEquals(response, output);

            verify(categoryMapper).toCategory(request);
            verify(categoryRepository).save(category);
            verify(categoryMapper).toCategoryResponse(category);
        }
    }

    @Nested
    class allCategories{

        @Test
        @DisplayName("Should list all categories with sucesso")
        void  shouldAllCategories(){

            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Viajem", CategoryType.DESPESA, "#ffffff");
            Category category2 = new Category(user, "Lazer", CategoryType.DESPESA, "#008000");
            Category category3 = new Category(user, "Porquinho", CategoryType.RECEITA, "#000000");

            List<Category> categories = List.of(category, category2, category3);

            CategoryResponse response =
                    new CategoryResponse(categoryId,"Viajem", CategoryType.DESPESA, "#ffffff");
            CategoryResponse response2 =
                    new CategoryResponse(categoryId,"Lazer", CategoryType.DESPESA, "#008000");
            CategoryResponse response3 =
                    new CategoryResponse(categoryId,"Porquinho", CategoryType.RECEITA, "#000000");

            when(categoryMapper.toCategoryResponse(category)).thenReturn(response);
            when(categoryMapper.toCategoryResponse(category2)).thenReturn(response2);
            when(categoryMapper.toCategoryResponse(category3)).thenReturn(response3);
            when(categoryRepository.findByUserId(userId)).thenReturn(categories);

            List<CategoryResponse> output = categoryService.getAllCategories(userId, "outracoisa");

            assertNotNull(output);

            verify(categoryMapper, times(3)).toCategoryResponse(any(Category.class));
        }
    }

}