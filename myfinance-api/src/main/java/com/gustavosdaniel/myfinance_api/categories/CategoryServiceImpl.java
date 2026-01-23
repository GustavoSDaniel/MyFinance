package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
