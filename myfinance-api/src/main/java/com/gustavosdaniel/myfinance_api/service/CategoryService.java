package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.controller.metrics.CategoryMetrics;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável pelas regras de negócio relacionadas às categorias.
 *
 * <p>As categorias pertencem exclusivamente ao usuário autenticado e podem ser
 * utilizadas para classificar receitas e despesas.
 *
 * <p>Esta classe gerencia operações como:
 * <ul>
 *     <li>Criação de categorias</li>
 *     <li>Busca e listagem</li>
 *     <li>Atualização de dados</li>
 *     <li>Ativação e desativação</li>
 *     <li>Remoção de categorias</li>
 * </ul>
 *
 * <p>Algumas consultas utilizam cache para melhorar a performance da aplicação.
 */
@Service
@CacheConfig(cacheNames = "categories")
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final AuthHelper authHelper;
    private final Logger log = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryMetrics categoryMetrics;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper, AuthHelper authHelper, CategoryMetrics categoryMetrics) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.authHelper = authHelper;
        this.categoryMetrics = categoryMetrics;
    }

    /**
     * Cria uma nova categoria para o usuário autenticado.
     *
     * <p>Antes da criação é verificado se já existe uma categoria com o mesmo nome
     * e tipo para o usuário. Caso exista, uma exceção será lançada.
     *
     * @param jwt usuário autenticado via OAuth2
     * @param request dados da categoria a ser criada
     * @return resposta contendo a categoria criada e a URI do recurso
     * @throws CategoryNameDuplicateException caso já exista uma categoria com o mesmo nome
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<CategoryResponse> createCategory(Jwt jwt,
                                                           CategoryRequest request){

        User user = authHelper.getCurrentUser(jwt);

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

        categoryMetrics.incrementCreated();

        return ResponseEntity.created(uri).body(categoryMapper.toCategoryResponse(saveCategory));
    }

    /**
     * Retorna todas as categorias do usuário autenticado.
     *
     * <p>É possível filtrar as categorias pelo status:
     * <ul>
     *     <li>active - categorias ativas</li>
     *     <li>disabled - categorias desativadas</li>
     *     <li>null ou vazio - todas as categorias</li>
     * </ul>
     *
     * @param jwt usuário autenticado
     * @param status filtro de status das categorias
     * @return lista de categorias encontradas
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "#jwt.subject + '_' + #status")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            Jwt jwt, String status) {

        User user = authHelper.getCurrentUser(jwt);

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

    /**
     * Realiza busca de categorias pelo nome.
     *
     * <p>A busca é limitada apenas às categorias pertencentes ao usuário autenticado.
     *
     * @param jwt usuário autenticado
     * @param name nome ou parte do nome da categoria
     * @return lista de categorias que correspondem ao critério de busca
     */
    @Transactional(readOnly = true)
    public ResponseEntity<List<CategoryResponse>> searchByName(Jwt jwt, String name) {

        log.info("Buscando categoria pelo nome {}", name);

        User user = authHelper.getCurrentUser(jwt);

        List<Category> categories = categoryRepository.searchByName(name, user.getId());

        log.info("Categorias {} encontradas com sucesso", categories.size());

        return ResponseEntity.ok(
                categories.stream()
                        .map(categoryMapper::toCategoryResponse)
                        .toList());
    }

    /**
     * Busca uma categoria específica pelo ID.
     *
     * <p>A categoria deve pertencer ao usuário autenticado.
     *
     * @param id identificador da categoria
     * @param jwt usuário autenticado
     * @return dados da categoria encontrada
     * @throws CategoryNotFoundException caso a categoria não exista
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "{#id, #jwt.subject}")
    public ResponseEntity<CategoryResponse> getById(UUID id, Jwt jwt) {

        log.info("Buscando categoria através do id: {}", id);

        User user = authHelper.getCurrentUser(jwt);

        Category category = categoryRepository
                .findByIdAndUserId(id, user.getId()).orElseThrow(CategoryNotFoundException::new);

        log.info("Categoria: {}, encontrado com sucesso", category.getName());

        return ResponseEntity.ok(categoryMapper.toCategoryResponse(category));
    }

    /**
     * Atualiza os dados de uma categoria existente.
     *
     * <p>Se o nome ou tipo da categoria for alterado, é verificado se já existe
     * outra categoria com o mesmo nome e tipo para o usuário.
     *
     * @param id identificador da categoria
     * @param jwt usuário autenticado
     * @param request novos dados da categoria
     * @return categoria atualizada
     * @throws CategoryNotFoundException caso a categoria não exista
     * @throws CategoryNameDuplicateException caso já exista uma categoria com o mesmo nome
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<CategoryResponseUpdate> updateCategory(
            UUID id, Jwt jwt, CategoryRequestUpdate request) {

        log.info("Atualizando categoria {} ", id);

        User user = authHelper.getCurrentUser(jwt);

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

        categoryMetrics.incrementUpdate();

        return ResponseEntity.ok(categoryMapper.toCategoryResponseUpdate(savedCategory));
    }

    /**
     * Desativa uma categoria ativa.
     *
     * @param id identificador da categoria
     * @param jwt usuário autenticado
     * @return resposta indicando sucesso na operação
     * @throws CategoryNotFoundException caso a categoria não exista
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deactivateCategory(UUID id, Jwt jwt) {

        User user = authHelper.getCurrentUser(jwt);

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

    /**
     * Ativa uma categoria que estava desativada.
     *
     * @param id identificador da categoria
     * @param jwt usuário autenticado
     * @return resposta indicando sucesso na operação
     * @throws CategoryNotFoundException caso a categoria não exista
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> activateCategory(UUID id, Jwt jwt) {

        User user = authHelper.getCurrentUser(jwt);

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


    /**
     * Deleta a categoria do sistema.
     *
     * <p>A categoria deve pertencer ao usuário autenticado.
     *
     * @param id identificador da categoria
     * @param jwt usuário autenticado
     * @return resposta indicando sucesso na remoção
     * @throws CategoryNotFoundException caso a categoria não exista
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deleteCategory(UUID id, Jwt jwt) {

        log.info("Solicitação para deletar categoria {}", id);

        User user = authHelper.getCurrentUser(jwt);

        Category category = categoryRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(CategoryNotFoundException::new);

        user.removeCategory(category);
        categoryRepository.delete(category);

        log.info("Categoria {} deletada com sucesso", category.getName());

        categoryMetrics.incrementDelete();

        return ResponseEntity.noContent().build();
    }
}
