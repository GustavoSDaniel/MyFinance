package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.exception.CategoryNameDuplicateException;
import com.gustavosdaniel.myfinance_api.exception.CategoryNotFoundException;
import com.gustavosdaniel.myfinance_api.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável por encapsular as regras de negócio relacionadas ao gerenciamento de categorias.
 * Gerencia a criação, atualização, busca, alteração de status e exclusão de categorias,
 * com suporte a cache para otimização de consultas.
 */
@Service
@CacheConfig(cacheNames = "categories")
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final Logger log = LoggerFactory.getLogger(CategoryService.class);

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    /**
     * Cria uma nova categoria e a vincula ao usuário informado.
     * Limpa o cache de categorias após a criação.
     *
     * @param user    Entidade do usuário logado que será o dono da categoria.
     * @param request DTO contendo os dados para criação da nova categoria.
     * @return DTO contendo as informações da categoria recém-criada.
     * @throws CategoryNameDuplicateException Caso o usuário já possua uma categoria com o mesmo nome e tipo.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public CategoryResponse createCategory(User user, CategoryRequest request){

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

        log.info("Categoria: {} criada com sucesso", saveCategory.getName());

        return categoryMapper.toCategoryResponse(saveCategory);
    }

    /**
     * Retorna uma lista de categorias de um usuário com base no status solicitado.
     * O resultado desta operação é armazenado em cache.
     *
     * @param userId ID do usuário dono das categorias.
     * @param status Status para filtro ("active", "disabled" ou qualquer outro valor para buscar todas).
     * @return Lista de DTOs com as informações das categorias encontradas.
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "#userId + '_' + #status")
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

        log.info("Total de categorias encontrados: {}", categories.size());

        return categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    /**
     * Realiza a busca de categorias pelo nome para um usuário específico.
     *
     * @param userId ID do usuário dono das categorias.
     * @param name   Parte ou nome completo da categoria a ser pesquisada.
     * @return Lista de DTOs correspondentes aos resultados da busca.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> searchByName(UUID userId, String name) {

        log.info("Buscando categoria pelo nome");

        List<Category> categories = categoryRepository.searchByName(name, userId);

        log.info("Categorias {} encontradas com sucesso", categories.size());

        return categories.stream().map(categoryMapper::toCategoryResponse).toList();
    }

    /**
     * Busca os detalhes de uma categoria específica pelo seu ID e ID do dono.
     * O resultado desta operação é armazenado em cache.
     *
     * @param id     ID da categoria a ser buscada.
     * @param userId ID do usuário dono da categoria.
     * @return DTO com as informações detalhadas da categoria.
     * @throws CategoryNotFoundException Caso a categoria não exista ou não pertença ao usuário.
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "{#id, #userId}")
    public CategoryResponse getById(UUID id, UUID userId) {

        log.info("Buscando categoria através do id: {}", id);

        Category category = categoryRepository
                .findByIdAndUserId(id, userId).orElseThrow(CategoryNotFoundException::new);

        log.info("Categoria: {}, encontrado com sucesso", category.getName());

        return categoryMapper.toCategoryResponse(category);
    }

    /**
     * Atualiza os dados de uma categoria existente.
     * Limpa o cache de categorias após a atualização.
     *
     * @param id      ID da categoria a ser atualizada.
     * @param userId  ID do usuário dono da categoria.
     * @param request DTO contendo os novos dados da categoria.
     * @return DTO com as informações atualizadas da categoria.
     * @throws CategoryNotFoundException      Caso a categoria não exista.
     * @throws CategoryNameDuplicateException Caso a combinação do novo nome e tipo já exista para este usuário.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public CategoryResponseUpdate updateCategory(UUID id, UUID userId,
                                                 CategoryRequestUpdate request)

    {

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

        log.info("Categoria {} atualizada com sucesso", savedCategory.getId());

        return categoryMapper.toCategoryResponseUpdate(savedCategory);
    }

    /**
     * Altera o status de uma categoria para inativa.
     * Ignora a requisição e apenas registra um aviso caso a categoria já esteja inativa.
     * Limpa o cache de categorias após a alteração.
     *
     * @param id     ID da categoria a ser desativada.
     * @param userId ID do usuário dono da categoria.
     * @throws CategoryNotFoundException Caso a categoria não exista.
     */
    @Transactional
    @CacheEvict(allEntries = true)
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

    /**
     * Altera o status de uma categoria para ativa.
     * Ignora a requisição e apenas registra um aviso caso a categoria já esteja ativa.
     * Limpa o cache de categorias após a alteração.
     *
     * @param id     ID da categoria a ser ativada.
     * @param userId ID do usuário dono da categoria.
     * @throws CategoryNotFoundException Caso a categoria não exista.
     */
    @Transactional
    @CacheEvict(allEntries = true)
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

    /**
     * Remove permanentemente uma categoria da base de dados e a desvincula da entidade do usuário.
     * Limpa o cache de categorias após a exclusão.
     *
     * @param id   ID da categoria a ser excluída.
     * @param user Entidade do usuário dono da categoria, necessária para remover o vínculo na memória.
     * @throws CategoryNotFoundException Caso a categoria não exista.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteCategory(UUID id, User user) {

        log.info("Solicitação para deletar categoria {}", id);

        Category category = categoryRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(CategoryNotFoundException::new);

        user.removeCategory(category);
        categoryRepository.delete(category);

        log.info("Categoria {} deletada com sucesso", category.getName());
    }
}
