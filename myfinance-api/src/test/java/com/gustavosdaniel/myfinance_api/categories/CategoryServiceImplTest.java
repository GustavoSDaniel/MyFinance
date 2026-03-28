package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.controller.metrics.CategoryMetrics;
import com.gustavosdaniel.myfinance_api.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.CategoryRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.CategoryResponseUpdate;
import com.gustavosdaniel.myfinance_api.domain.enuns.CategoryType;
import com.gustavosdaniel.myfinance_api.domain.mapping.CategoryMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.exception.CategoryNameDuplicateException;
import com.gustavosdaniel.myfinance_api.repository.CategoryRepository;
import com.gustavosdaniel.myfinance_api.service.CategoryService;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.domain.enuns.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
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

    @Mock
    private CategoryMetrics categoryMetrics;

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    class createCategory{

        @Test
        @DisplayName("Should created category with sucesso")
        void shouldCreateCategory(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID categoryId = UUID.randomUUID();

            User user = new User(keycloakId,"gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            CategoryRequest request = new CategoryRequest("Lazer", CategoryType.DESPESA, "#008000");

            Category category = new Category(user, "Lazer", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            CategoryResponse response =
                    new CategoryResponse(categoryId,"Lazer", CategoryType.DESPESA, "#008000");


            when(categoryRepository.existsByNameIgnoreCaseAndUserIdAndType(
                    category.getName().trim(), userId, category.getType())).thenReturn(false);
            when(categoryMapper.toCategory(user, request)).thenReturn(category);
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toCategoryResponse(category)).thenReturn(response);

            categoryMetrics.incrementCreated();

            CategoryResponse output = categoryService.createCategory(user, request);

            assertNotNull(output);
            assertEquals(response, output);

            verify(categoryMapper).toCategory(user, request);
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
            String keycloakId = "idDoKeycloak";
            UUID categoryId = UUID.randomUUID();

            User user = new User(keycloakId,"gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
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

    @Nested
    class searchName{

        @Test
        @DisplayName("Should search name with sucesso")
        void shouldName(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID categoryId = UUID.randomUUID();

            User user = new User(keycloakId,"gustavosdaniel@hotmail.com", "Gustavo", UserRole.USER);
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
            when(categoryRepository.searchByName(category2.getName(), userId)).thenReturn(categories);

            List<CategoryResponse> output = categoryService.searchByName(userId, response2.name());

            assertNotNull(output);

            verify(categoryMapper, times(3)).toCategoryResponse(any(Category.class));
            verify(categoryRepository).searchByName(category2.getName(), userId);
        }
    }

    @Nested
    class findBtId{

        @Test
        @DisplayName("Should category by ID with sucesso")
        void shouldCategoryById(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID categoryId = UUID.randomUUID();

            User user = new User(keycloakId,"gustavosdaniel@hotmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Lazer", CategoryType.DESPESA, "000000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            CategoryResponse response =
                    new CategoryResponse(categoryId, "Lazer", CategoryType.DESPESA, "000000");

            when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
            when(categoryMapper.toCategoryResponse(category)).thenReturn(response);

            CategoryResponse output = categoryService.getById(categoryId, userId);

            assertNotNull(output);
            assertEquals(response, output);

            verify(categoryRepository).findByIdAndUserId(categoryId, userId);
            verify(categoryMapper).toCategoryResponse(category);
        }
    }

    @Nested
    class updateCategory{

        @Test
        @DisplayName("Should update category with sucesso")
        void shouldUpdateCategory() throws CategoryNameDuplicateException {

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID categoryId = UUID.randomUUID();

            User user = new User(keycloakId,"gustavosdaniel@hotmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Lazer", CategoryType.DESPESA, "000000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            CategoryRequestUpdate request = new CategoryRequestUpdate(
                    "Investimento",
                    CategoryType.RECEITA,
                    "FFFFFF",
                    "Investimento para comprar o priomeiro carro",
                    "caminho da imagem do icone");

            CategoryResponseUpdate response = new CategoryResponseUpdate(
                    categoryId,
                    "Investimento",
                    CategoryType.RECEITA,
                    "FFFFFF",
                    "Investimento para comprar o priomeiro carro",
                    "caminho da imagem do icone");

            when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toCategoryResponseUpdate(category)).thenReturn(response);

            CategoryResponseUpdate output = categoryService.updateCategory(categoryId, userId, request);

            assertNotNull(output);
            assertEquals(response, output);

            verify(categoryRepository).findByIdAndUserId(categoryId, userId);
            verify(categoryRepository).save(category);
            verify(categoryMapper).toCategoryResponseUpdate(category);

        }
    }

    @Nested
    class deactivateCategory{

        @Test
        @DisplayName("Should deactivate category with sucesso")
        void shouldDeactivateCategory(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID categoryId = UUID.randomUUID();
            Boolean isActive = true;

            User user = new User(keycloakId,"gustavosdaniel@hotmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Lazer", CategoryType.DESPESA, "000000");
            ReflectionTestUtils.setField(category, "id", categoryId);
            ReflectionTestUtils.setField(category, "isActive", isActive);

            when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            categoryService.deactivateCategory(categoryId, userId);

            verify(categoryRepository).findByIdAndUserId(categoryId, userId);
            verify(categoryRepository).save(category);

        }
    }

    @Nested
    class activateCategory{

        @Test
        @DisplayName("Should activate category with sucesso")
        void shouldActivateCategory(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID categoryId = UUID.randomUUID();
            Boolean isActive = false;

            User user = new User(keycloakId,"gustavosdaniel@hotmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Lazer", CategoryType.DESPESA, "000000");
            ReflectionTestUtils.setField(category, "id", categoryId);
            ReflectionTestUtils.setField(category, "isActive", isActive);

            when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            categoryService.activateCategory(categoryId, userId);

            verify(categoryRepository).findByIdAndUserId(categoryId, userId);
            verify(categoryRepository).save(category);

        }
    }

    @Nested
    class deleteCategory{

        @Test
        @DisplayName("Should delete category with sucesso")
        void shouldDeleteCategory(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID categoryId = UUID.randomUUID();

            User user = new User(keycloakId,"gustavosdaniel@hotmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Lazer", CategoryType.DESPESA, "000000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));

            categoryService.deleteCategory(categoryId, user);

            categoryMetrics.incrementDelete();

            verify(categoryRepository).findByIdAndUserId(categoryId, userId);
        }
    }

}