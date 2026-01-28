package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public List<CategoryResponse> getAllCategories(UUID userId, String status) {

        List<Category> categories;

        if ("active".equalsIgnoreCase(status)){

            log.info("Buscando categorias ativas do usuário: {}", userId);

            categories = categoryRepository.findByUserIdAndIsActiveTrue(userId);

        } else if ("disabled".equalsIgnoreCase(status)) {

            log.info("Buscando categorias desativadas do usuário: {}", userId);

            categories = categoryRepository.findByUserIdAndIsActiveFalse(userId);

        } else {

            log.info("Buscando todas as categorias do usuário: {}", userId);

            categories = categoryRepository.findByUserId(userId);
        }

        log.info("Total encontrado: {}", categories.size());

        return categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> searchByName(UUID userId, String name) {

        log.info("Buscando categoria pelo nome");

        List<Category> categories = categoryRepository.searchByName(name, userId);

        log.info("Categorias {} encontradas com sucesso", categories.size());

        return categories.stream().map(categoryMapper::toCategoryResponse).toList();
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

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, UUID userId, CategoryRequestUpdate request)
            throws CategoryNameDuplicateException {

        log.info("Atualizando categoria {} ", id);

        Category category = categoryRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(CategoryNotFoundException::new);

        CategoryType typeToCheck = request.type() != null ? request.type() : category.getType();

        if (request.name() != null && !request.name().equalsIgnoreCase(category.getName())){

            if (categoryRepository
                    .existsByNameIgnoreCaseAndUserIdAndType(request.name(), userId, typeToCheck)){

                throw new CategoryNameDuplicateException();
            }
        }

        categoryMapper.toCategoryUpdate(category, request);

        Category savedCategory = categoryRepository.save(category);

        log.info("Categoria {} atualizada com sucesso", savedCategory.getName());

        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public void deactivateCategory(UUID id, UUID userId) {

        log.info("Desativando categoria {} do usuário: {}",id, userId);

        Category category = categoryRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(CategoryNotFoundException::new);

        if (Boolean.FALSE.equals(category.getIsActive())){

            log.warn("Categoria {} já está desativada", category.getName());

            return;
        }

        category.deactivate();

        categoryRepository.save(category);

        log.info("Categoria: {} desativada com sucesso", category.getName());
    }

    @Override
    @Transactional
    public void activateCategory(UUID id, UUID userId) {

        log.info("Ativando categoria {} do usuário: {} ", id, userId);

        Category category = categoryRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(CategoryNotFoundException::new);

        if (Boolean.TRUE.equals(category.getIsActive())){

            log.warn("Categoria {} já está ativada", category.getName());

            return;
        }

        category.activate();

        categoryRepository.save(category);

        log.info("Categoria: {}, ativada com sucesso", category.getName());

    }

    @Override
    @Transactional
    public void deleteCategory(UUID id, UUID userId) {

        log.info("Solicitação para deletar categoria {}", id);

        Category category = categoryRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(CategoryNotFoundException::new);

        categoryRepository.delete(category);

        log.info("Categoria {} deletada com sucesso", category.getName());
    }


}
