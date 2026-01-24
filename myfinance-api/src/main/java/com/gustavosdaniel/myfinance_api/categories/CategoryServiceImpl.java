package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(User user, CategoryRequest request) throws CategoryNameDuplicateException {

        log.info("Criando categoria para usuário {}", user.getName());

        if (categoryRepository.existsByNameIgnoreCaseAndUserIdAndType(
                request.name().trim(),
                user.getId(),
                request.type())
        ){

            throw new CategoryNameDuplicateException();
        }

        Category newCategory = categoryMapper.toCategory(request);
        newCategory.setUser(user);

        Category saveCategory = categoryRepository.save(newCategory);

        log.info("Categoria: {} salva com sucesso", saveCategory.getName());

        return categoryMapper.toCategoryResponse(saveCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id, UUID userId) {

        log.info("Buscando categoria através do id: {}", id);

        Category category = categoryRepository
                .findByIdAndUserId(id, userId).orElseThrow(CategoryNotFoundException::new);

        log.info("Categoria: {}, encontrado com sucesso", category.getName());

        return categoryMapper.toCategoryResponse(category);
    }
}
