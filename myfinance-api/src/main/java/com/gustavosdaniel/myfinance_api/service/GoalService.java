package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.controller.metrics.GoalMetrics;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalTransfer;
import com.gustavosdaniel.myfinance_api.domain.mapping.GoalMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.domain.po.Goal;
import com.gustavosdaniel.myfinance_api.exception.AccountNotFoundException;
import com.gustavosdaniel.myfinance_api.repository.AccountRepository;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.exception.CategoryNotFoundException;
import com.gustavosdaniel.myfinance_api.repository.CategoryRepository;
import com.gustavosdaniel.myfinance_api.exception.GoalNameDuplicateException;
import com.gustavosdaniel.myfinance_api.exception.GoalNotFoundException;
import com.gustavosdaniel.myfinance_api.exception.IdempotencyKeyException;
import com.gustavosdaniel.myfinance_api.repository.GoalRepository;
import com.gustavosdaniel.myfinance_api.domain.po.Transaction;
import com.gustavosdaniel.myfinance_api.repository.TransactionRepository;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável pelas regras de negócio relacionadas às metas financeiras.
 *
 * <p>Uma meta representa um objetivo financeiro definido pelo usuário,
 * como por exemplo: viajar, comprar um carro....
 *
 * <p>Este serviço gerencia operações como:
 * <ul>
 *     <li>Criação de metas</li>
 *     <li>Busca e listagem</li>
 *     <li>Atualização de metas</li>
 *     <li>Depósitos em metas</li>
 *     <li>Resgate de valores de metas</li>
 *     <li>Remoção de metas</li>
 * </ul>
 *
 * <p>Todos os dados são vinculados ao usuário autenticado.
 * Algumas consultas utilizam cache para melhorar a performance.
 */
@Service
@CacheConfig(cacheNames = "goals")
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuthHelper authHelper;
    private final Logger log = LoggerFactory.getLogger(GoalService.class);
    private final GoalMetrics goalMetrics;

    public GoalService(GoalRepository goalRepository, GoalMapper goalMapper, CategoryRepository categoryRepository, AccountRepository accountRepository, TransactionRepository transactionRepository, AuthHelper authHelper, GoalMetrics goalMetrics) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.authHelper = authHelper;
        this.goalMetrics = goalMetrics;
    }

    /**
     * Cria uma nova meta financeira para o usuário autenticado.
     *
     * <p>Antes da criação é verificado se já existe uma meta com o mesmo nome
     * para o usuário. Caso exista, uma exceção será lançada.
     *
     * @param jwt usuário autenticado
     * @param request dados da meta a ser criada
     * @return resposta contendo a meta criada e a URI do recurso
     * @throws GoalNameDuplicateException caso já exista uma meta com o mesmo nome
     * @throws CategoryNotFoundException caso a categoria não exista
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<GoalResponse> createGoal(Jwt jwt, GoalRequest request){

        User user = authHelper.getCurrentUser(jwt);

        log.info("Criando Meta para o usuário: {}", user.getName());

        if (goalRepository.existsByNameIgnoreCaseAndUserId(request.name().trim(), user.getId())){

            throw new GoalNameDuplicateException();
        }

        Category category = categoryRepository
                .findByIdAndUserId(request.categoryId(), user.getId()).orElseThrow(CategoryNotFoundException::new);

        Goal newGoal = goalMapper.toGoal(request, user, category);
        category.addGoal(newGoal);

        Goal saveGoal = goalRepository.save(newGoal);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saveGoal.getId())
                .toUri();

        log.info("Meta criado com sucesso: {}", saveGoal.getName());

        goalMetrics.incrementCreated();

        return ResponseEntity.created(uri).body(goalMapper.toGoalResponse(saveGoal));
    }

    /**
     * Busca uma meta específica pelo ID.
     *
     * <p>A meta deve pertencer ao usuário autenticado.
     *
     * @param id identificador da meta
     * @param jwt usuário autenticado
     * @return dados da meta encontrada
     * @throws GoalNotFoundException caso a meta não exista
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "{#id, #jwt.subject}")
    public ResponseEntity<GoalResponse> getGoalById(UUID id, Jwt jwt) {

        log.info("Buscando Meta pelo id {}", id);

        User user = authHelper.getCurrentUser(jwt);

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(GoalNotFoundException::new);

        log.info("Meta {} encontrada com sucesso", goal.getName());

        return ResponseEntity.ok(goalMapper.toGoalResponse(goal));
    }

    /**
     * Busca metas pelo nome.
     *
     * <p>A busca é limitada às metas pertencentes ao usuário autenticado.
     *
     * @param jwt usuário autenticado
     * @param name nome ou parte do nome da meta
     * @return lista de metas encontradas
     */
    @Transactional(readOnly = true)
    public ResponseEntity<List<GoalResponse>> searchGoal(Jwt jwt, String name) {

        log.info("Buscando Metas pelo nome {}", name);

        User user = authHelper.getCurrentUser(jwt);

        List<Goal> goals = goalRepository.searchName(name, user.getId());

        if (goals.isEmpty()){

            log.warn("Nenhuma Meta desse usuário {}  encontrado com esse nome {}", user.getName(), name);

            return ResponseEntity.noContent().build();
        }

        log.info("Total de Metas encontrados {}", goals.size());

        return ResponseEntity.ok(
                goals.stream()
                        .map(goalMapper::toGoalResponse)
                        .toList());
    }

    /**
     * Retorna uma lista paginada de metas do usuário.
     *
     * <p>É possível filtrar as metas pelo status:
     * <ul>
     *     <li>achieved - metas já alcançadas</li>
     *     <li>progress - metas ainda em progresso</li>
     *     <li>null ou vazio - todas as metas</li>
     * </ul>
     *
     * @param jwt usuário autenticado
     * @param status filtro de status das metas
     * @param pageable informações de paginação
     * @return página contendo as metas encontradas
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<GoalResponse>> getAllGoals(
            Jwt jwt, String status,  Pageable pageable) {

        User user = authHelper.getCurrentUser(jwt);

        log.info("Buscando todas as metas do usuário {}", user.getName());

        Page<Goal> goals;

        if ("achieved".equalsIgnoreCase(status)){

            goals = goalRepository.findAchievedGoals(user.getId(), pageable);

            log.info("Todos as Metas já alcançados {}", goals.getTotalElements());

        } else if ("progress".equalsIgnoreCase(status)) {

            goals = goalRepository.findPendingGoals(user.getId(), pageable);

            log.info("Todos as Metas  em progresso {}", goals.getTotalElements());


        } else {
            goals = goalRepository.findByUserId(user.getId(), pageable);

            log.info("Todos as Metas encontrados {}", goals.getTotalElements());
        }

        if (goals.isEmpty()){

            log.info("Nenhuma Meta foi encontrado");

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(goals.map(goalMapper::toGoalResponse));
    }

    /**
     * Atualiza os dados de uma meta existente.
     *
     * <p>Se o nome da meta for alterado, é verificado se já existe
     * outra meta com o mesmo nome para o usuário.
     *
     * @param id identificador da meta
     * @param requestUpdate novos dados da meta
     * @param jwt usuário autenticado
     * @return meta atualizada
     * @throws GoalNotFoundException caso a meta não exista
     * @throws GoalNameDuplicateException caso já exista uma meta com o mesmo nome
     * @throws CategoryNotFoundException caso a nova categoria não exista
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<GoalResponse> updateGoal(
            UUID id, GoalRequestUpdate requestUpdate, Jwt jwt) {

        User user = authHelper.getCurrentUser(jwt);

        log.info("Atualizando Meta {}, do usuário {}", id, user.getName());

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        Category category = null;

        if (requestUpdate.categoryId() != null) {

           category = categoryRepository
                    .findByIdAndUserId(requestUpdate.categoryId(), user.getId())
                    .orElseThrow(CategoryNotFoundException::new);

        }

        if (requestUpdate.name() != null && !requestUpdate.name().equalsIgnoreCase(goal.getName())) {

            if (goalRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(requestUpdate.name(), user.getId(), id)) {

                throw new GoalNameDuplicateException();

            }
        }


        goalMapper.toGoalUpdate(requestUpdate, goal, category);

        Goal saveGoal = goalRepository.save(goal);

        log.info("Meta atualizada com sucesso {}", saveGoal.getName());

        goalMetrics.incrementUpdate();

        return ResponseEntity.ok(goalMapper.toGoalResponse(saveGoal));
    }

    /**
     * Realiza um depósito de dinheiro em uma meta financeira.
     *
     * <p>O valor é transferido de uma conta do usuário para a meta.
     * Uma transação financeira é criada para registrar a movimentação.
     *
     * <p>Também é utilizada uma {@code idempotencyKey} para evitar
     * duplicação da operação.
     *
     * @param id identificador da meta
     * @param transfer dados da transferência
     * @param jwt usuário autenticado
     * @return meta atualizada após o depósito
     * @throws GoalNotFoundException caso a meta não exista
     * @throws AccountNotFoundException caso a conta não exista
     * @throws IdempotencyKeyException caso a transação já tenha sido processada
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<GoalResponse> depositToGoal(
            UUID id, GoalTransfer transfer, Jwt jwt) {

        log.info("Realizando transação da conta {}, para a Meta {}", transfer.accountId(), id);

        User user = authHelper.getCurrentUser(jwt);

        if (transactionRepository.existsByIdempotencyKeyAndUserId(transfer.idempotencyKey(),
                user.getId())){

            log.warn("Transação já processada anteriormente. idempotencyKey = {}",
                    transfer.idempotencyKey());

            throw new IdempotencyKeyException();
        }

        Goal goal = goalRepository
                .findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        Account account = accountRepository
                .findByIdAndUserId(transfer.accountId(), user.getId())
                .orElseThrow(AccountNotFoundException::new);

        Transaction transaction = new Transaction(
                transfer.idempotencyKey(),
                user,
                account,
                goal.getCategory(),
                transfer.description(),
                transfer.amount(),
                TransactionType.DESPESA,
                LocalDateTime.now(),
                null,
                null
        );

        goal.addAmount(transfer.amount());
        transaction.process();

        Goal saveGoal = goalRepository.save(goal);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        log.info("Transação para a Meta {} realizada com sucesso", goal.getName());

        return ResponseEntity.ok(goalMapper.toGoalResponse(saveGoal));
    }

    /**
     * Realiza o resgate de dinheiro de uma meta financeira para uma conta do usuário.
     *
     * <p>O valor é removido da meta e depositado na conta selecionada,
     * sendo registrada uma transação financeira.
     *
     * <p>Também utiliza {@code idempotencyKey} para evitar duplicação.
     *
     * @param id identificador da meta
     * @param transfer dados do resgate
     * @param jwt usuário autenticado
     * @return meta atualizada após o resgate
     * @throws GoalNotFoundException caso a meta não exista
     * @throws AccountNotFoundException caso a conta não exista
     * @throws IdempotencyKeyException caso a transação já tenha sido processada
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<GoalResponse> withdrawFromGoal(
            UUID id, GoalTransfer transfer, Jwt jwt) {

        log.info("Resgatando valor do Goal {} para a conta {}", id, transfer.accountId());

        User user = authHelper.getCurrentUser(jwt);

        if (transactionRepository.existsByIdempotencyKeyAndUserId(transfer.idempotencyKey(),
                user.getId())){

            log.warn("Transação já processada anteriormente. idempotencyKey = {}",
                    transfer.idempotencyKey());

            throw new IdempotencyKeyException();
        }

        Goal goal = goalRepository
                .findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        Account account = accountRepository
                .findByIdAndUserId(transfer.accountId(), user.getId())
                .orElseThrow(AccountNotFoundException::new);

        Transaction transaction = new Transaction(
                transfer.idempotencyKey(),
                user,
                account,
                goal.getCategory(),
                transfer.description(),
                transfer.amount(),
                TransactionType.RECEITA,
                LocalDateTime.now(),
                null,
                null
        );

        goal.removeAmount(transfer.amount());
        transaction.process();

        Goal saveGoal = goalRepository.save(goal);
        accountRepository.save(account);
        transactionRepository.save(transaction);

        log.info("Resgate no valor de {} realizado com sucesso para a conta {}",
                transfer.amount(), account.getName());
        return ResponseEntity.ok(goalMapper.toGoalResponse(saveGoal));
    }

    /**
     * Remove uma meta do sistema.
     *
     * <p>Uma meta só pode ser removida caso não possua saldo.
     * Caso exista valor armazenado na meta, o usuário deverá
     * resgatar o valor antes de removê-la.
     *
     * @param id identificador da meta
     * @param jwt usuário autenticado
     * @return resposta indicando sucesso na operação
     * @throws GoalNotFoundException caso a meta não exista
     * @throws IllegalArgumentException caso a meta possua saldo
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deleteGoal(UUID id, Jwt jwt) {

        log.info("Deletando Meta {}", id);

        User user = authHelper.getCurrentUser(jwt);

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        if (goal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0){

            throw new IllegalArgumentException(
                    "Não é possível deletar a Meta pois ela contém saldo. " +
                            "Resgate o dinheiro antes.");
        }

        user.removeGoals(goal);
        goalRepository.delete(goal);

        log.info("Meta deletada com sucesso!");

        goalMetrics.incrementDelete();

        return ResponseEntity.noContent().build();
    }
}
