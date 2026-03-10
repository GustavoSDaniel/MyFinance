package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.domain.dto.CategoryRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.CategoryResponseUpdate;
import com.gustavosdaniel.myfinance_api.domain.enuns.CategoryType;
import com.gustavosdaniel.myfinance_api.domain.mapping.CategoryMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.exception.CategoryNameDuplicateException;
import com.gustavosdaniel.myfinance_api.exception.CategoryNotFoundException;
import com.gustavosdaniel.myfinance_api.repository.CategoryRepository;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
@CacheConfig(cacheNames = "categories")
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final AuthHelper authHelper;
    private final Logger log = LoggerFactory.getLogger(CategoryService.class);

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper, AuthHelper authHelper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.authHelper = authHelper;
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<CategoryResponse> createCategory(OAuth2User principal,
                                                           CategoryRequest request){

        User user = authHelper.getCurrentUser(principal);

        log.info("Criando categoria para usuário {}", user.getName());

        if (categoryRepository.existsByNameIgnoreCaseAndUserIdAndType(
                request.name().trim(),
                user.getId(),
                request.type())
        ){

            throw new CategoryNameDuplicateException();
        }

        Category newCategory = categoryMapper.toCategory(user, request);
        user.addCategory(newCategory);

        Category saveCategory = categoryRepository.save(newCategory);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saveCategory.getId())
                .toUri();

        log.info("Categoria: {} criada com sucesso", saveCategory.getName());

        return ResponseEntity.created(uri).body(categoryMapper.toCategoryResponse(saveCategory));
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#userId + '_' + #status")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            OAuth2User principal, String status) {

        User user = authHelper.getCurrentUser(principal);

        log.info("Busca todas as categorias criado pelo usuário {}", user.getName());

        List<Category> categories;

        if ("active".equalsIgnoreCase(status)){

            log.info("Buscando categorias ativas do usuário: {}", user.getId());

            categories = categoryRepository.findByUserIdAndIsActiveTrue(user.getId());

        } else if ("disabled".equalsIgnoreCase(status)) {

            log.info("Buscando categorias desativadas do usuário: {}", user.getId());

            categories = categoryRepository.findByUserIdAndIsActiveFalse(user.getId());

        } else {

            log.info("Buscando todas as categorias do usuário: {}", user.getId());

            categories = categoryRepository.findByUserId(user.getId());
        }

        log.info("Total de categorias encontrados: {}", categories.size());

        return ResponseEntity.ok(categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<CategoryResponse>> searchByName(OAuth2User principal, String name) {

        log.info("Buscando categoria pelo nome {}", name);

        User user = authHelper.getCurrentUser(principal);

        List<Category> categories = categoryRepository.searchByName(name, user.getId());

        log.info("Categorias {} encontradas com sucesso", categories.size());

        return ResponseEntity.ok(
                categories.stream()
                        .map(categoryMapper::toCategoryResponse)
                        .toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "{#id, #userId}")
    public ResponseEntity<CategoryResponse> getById(UUID id, OAuth2User principal) {

        log.info("Buscando categoria através do id: {}", id);

        User user = authHelper.getCurrentUser(principal);

        Category category = categoryRepository
                .findByIdAndUserId(id, user.getId()).orElseThrow(CategoryNotFoundException::new);

        log.info("Categoria: {}, encontrado com sucesso", category.getName());

        return ResponseEntity.ok(categoryMapper.toCategoryResponse(category));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<CategoryResponseUpdate> updateCategory(
            UUID id, OAuth2User principal, CategoryRequestUpdate request) {

        log.info("Atualizando categoria {} ", id);

        User user = authHelper.getCurrentUser(principal);

        Category category = categoryRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(CategoryNotFoundException::new);

        CategoryType typeToCheck = request.type() != null ? request.type() : category.getType();

        if (request.name() != null && !request.name().equalsIgnoreCase(category.getName())){

            if (categoryRepository
                    .existsByNameIgnoreCaseAndUserIdAndType(request.name(), user.getId(),
                            typeToCheck)){

                throw new CategoryNameDuplicateException();
            }
        }

        categoryMapper.toCategoryUpdate(category, request);

        Category savedCategory = categoryRepository.save(category);

        log.info("Categoria {} atualizada com sucesso", savedCategory.getId());

        return ResponseEntity.ok(categoryMapper.toCategoryResponseUpdate(savedCategory));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deactivateCategory(UUID id, OAuth2User principal) {

        User user = authHelper.getCurrentUser(principal);

        log.info("Desativando categoria {} do usuário: {}",id, user.getId());

        Category category = categoryRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(CategoryNotFoundException::new);

        if (Boolean.FALSE.equals(category.getIsActive())){

            log.warn("Categoria {} já está desativada", category.getName());

            return ResponseEntity.noContent().build();
        }

        category.deactivate();

        categoryRepository.save(category);

        log.info("Categoria: {} desativada com sucesso", category.getName());

        return ResponseEntity.noContent().build();
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> activateCategory(UUID id, OAuth2User principal) {

        User user = authHelper.getCurrentUser(principal);

        log.info("Ativando categoria {} do usuário: {} ", id, user.getId());

        Category category = categoryRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(CategoryNotFoundException::new);

        if (Boolean.TRUE.equals(category.getIsActive())){

            log.warn("Categoria {} já está ativada", category.getName());

            return ResponseEntity.noContent().build();
        }

        category.activate();

        categoryRepository.save(category);

        log.info("Categoria: {}, ativada com sucesso", category.getName());

        return ResponseEntity.noContent().build();

    }

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deleteCategory(UUID id, OAuth2User principal) {

        log.info("Solicitação para deletar categoria {}", id);

        User user = authHelper.getCurrentUser(principal);

        Category category = categoryRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(CategoryNotFoundException::new);

        user.removeCategory(category);
        categoryRepository.delete(category);

        log.info("Categoria {} deletada com sucesso", category.getName());

        return ResponseEntity.noContent().build();
    }
}
