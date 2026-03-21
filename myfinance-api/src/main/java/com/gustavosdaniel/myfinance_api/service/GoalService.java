package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.domain.dto.request.GoalRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.GoalRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.response.GoalResponse;
import com.gustavosdaniel.myfinance_api.domain.mapping.GoalMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.domain.po.Goal;
import com.gustavosdaniel.myfinance_api.exception.AccountNotFoundException;
import com.gustavosdaniel.myfinance_api.domain.dto.request.GoalTransferRequest;
import com.gustavosdaniel.myfinance_api.repository.AccountRepository;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.exception.CategoryNotFoundException;
import com.gustavosdaniel.myfinance_api.repository.CategoryRepository;
import com.gustavosdaniel.myfinance_api.exception.GoalNameDuplicateException;
import com.gustavosdaniel.myfinance_api.exception.GoalNotFoundException;
import com.gustavosdaniel.myfinance_api.exception.IdempotencyKeyException;
import com.gustavosdaniel.myfinance_api.controller.metrics.GoalMetrics;
import com.gustavosdaniel.myfinance_api.repository.GoalRepository;
import com.gustavosdaniel.myfinance_api.domain.po.Transaction;
import com.gustavosdaniel.myfinance_api.repository.TransactionRepository;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.exception.InsufficientBalanceException;
import com.gustavosdaniel.myfinance_api.util.InvalidAmountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável pelo gerenciamento de metas (Goals).
 * Lida com a criação, listagem, atualização, exclusão e movimentações financeiras
 * (depósitos e resgates) vinculadas a uma meta específica.
 */
@Service
@CacheConfig(cacheNames = "goals")
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Logger log = LoggerFactory.getLogger(GoalService.class);
    private final GoalMetrics goalMetrics;

    public GoalService(GoalRepository goalRepository, GoalMapper goalMapper, CategoryRepository categoryRepository, AccountRepository accountRepository, TransactionRepository transactionRepository, GoalMetrics goalMetrics) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.goalMetrics = goalMetrics;
    }

    /**
     * Cria uma nova meta para o usuário informado e a vincula a uma categoria existente.
     * Limpa o cache de metas após a criação.
     *
     * @param user    Entidade do usuário proprietário da meta.
     * @param request DTO com os dados de criação da meta.
     * @return DTO contendo as informações da meta criada.
     * @throws InvalidAmountException        Caso o valor inicial ou alvo seja inválido.
     * @throws GoalNameDuplicateException    Caso o usuário já possua uma meta com o mesmo nome.
     * @throws CategoryNotFoundException     Caso a categoria informada não exista.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public GoalResponse createGoal(User user, GoalRequest request){

        log.info("Criando Meta para o usuário: {}", user.getName());

        if (goalRepository.existsByNameIgnoreCaseAndUserId(request.name().trim(), user.getId())){

            throw new GoalNameDuplicateException();
        }

        Category category = categoryRepository
                .findByIdAndUserId(request.categoryId(), user.getId())
                .orElseThrow(CategoryNotFoundException::new);

        Goal newGoal = goalMapper.toGoal(request, user, category);
        category.addGoal(newGoal);

        Goal saveGoal = goalRepository.save(newGoal);

        goalMetrics.incrementCreated();

        log.info("Meta criado com sucesso: {}", saveGoal.getName());

        return goalMapper.toGoalResponse(saveGoal);
    }

    /**
     * Busca uma meta específica pelo seu ID e a valida com o ID do usuário dono.
     * O resultado desta operação é armazenado em cache.
     *
     * @param id   ID da meta a ser buscada.
     * @param user Entidade do usuário dono da meta.
     * @return DTO com os detalhes da meta.
     * @throws GoalNotFoundException Caso a meta não exista ou não pertença ao usuário.
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "{#id, #user.id}")
    public GoalResponse getGoalById(UUID id, User user) {

        log.info("Buscando Meta pelo id {}", id);

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(GoalNotFoundException::new);

        log.info("Meta {} encontrada com sucesso", goal.getName());

        return goalMapper.toGoalResponse(goal);
    }

    /**
     * Realiza uma busca de metas pelo nome para um usuário específico.
     *
     * @param user Entidade do usuário dono das metas.
     * @param name Nome parcial ou total a ser pesquisado.
     * @return Lista de DTOs contendo as metas que correspondem à busca.
     */
    @Transactional(readOnly = true)
    public List<GoalResponse> searchGoal(User user, String name) {

        log.info("Buscando Metas pelo nome {}", name);

        List<Goal> goals = goalRepository.searchName(name, user.getId());

        if (goals.isEmpty()){

            log.warn("Nenhuma Meta desse usuário {}  encontrado com esse nome {}",
                    user.getName(), name);

            return List.of();
        }

        log.info("Total de Metas encontrados {}", goals.size());

        return goals.stream().map(goalMapper::toGoalResponse).toList();
    }

    /**
     * Retorna uma lista paginada de metas de um usuário com base no status solicitado.
     *
     * @param user     Entidade do usuário dono das metas.
     * @param status   Filtro de status ("achieved" para alcançadas, "progress" para em andamento).
     * @param pageable Configurações de paginação.
     * @return Página contendo os DTOs das metas encontradas.
     */
    @Transactional(readOnly = true)
    public Page<GoalResponse> getAllGoals(User user, String status,  Pageable pageable) {

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

            return Page.empty();
        }

        return goals.map(goalMapper::toGoalResponse);
    }

    /**
     * Atualiza as informações de uma meta existente.
     * Limpa o cache de metas após a atualização.
     *
     * @param id            ID da meta a ser atualizada.
     * @param requestUpdate DTO contendo os novos dados para a meta.
     * @param user          Entidade do usuário dono da meta.
     * @return DTO com os dados da meta atualizada.
     * @throws GoalNotFoundException      Caso a meta não seja encontrada.
     * @throws CategoryNotFoundException  Caso uma nova categoria informada não seja encontrada.
     * @throws GoalNameDuplicateException Caso o novo nome já esteja em uso por outra meta do usuário.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public GoalResponse updateGoal(UUID id, GoalRequestUpdate requestUpdate, User user) {

        log.info("Atualizando Meta {}, do usuário {}", id, user.getName());

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(GoalNotFoundException::new);

        Category category = null;

        if (requestUpdate.categoryId() != null) {

           category = categoryRepository
                    .findByIdAndUserId(requestUpdate.categoryId(), user.getId())
                    .orElseThrow(CategoryNotFoundException::new);

        }

        if (requestUpdate.name() != null && !requestUpdate.name().equalsIgnoreCase(goal.getName())) {

            if (goalRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(requestUpdate.name()
                    , user.getId(), id)) {

                throw new GoalNameDuplicateException();

            }
        }


        goalMapper.toGoalUpdate(requestUpdate, goal, category);

        Goal saveGoal = goalRepository.save(goal);

        goalMetrics.incrementUpdate();

        log.info("Meta atualizada com sucesso {}", saveGoal.getName());

        return goalMapper.toGoalResponse(saveGoal);
    }

    /**
     * Realiza um depósito na meta, transferindo fundos de uma conta do usuário para ela.
     * Esta operação gera uma transação de "DESPESA" na conta (dinheiro saindo) para ser adicionado à meta.
     * Limpa o cache de metas.
     *
     * @param id       ID da meta que receberá o depósito.
     * @param transfer DTO contendo o valor, a conta de origem e a chave de idempotência.
     * @param user     Entidade do usuário logado.
     * @return DTO da meta atualizada com o novo saldo.
     * @throws IdempotencyKeyException       Caso uma transação com a mesma chave de idempotência já tenha sido processada.
     * @throws GoalNotFoundException         Caso a meta não seja encontrada.
     * @throws AccountNotFoundException      Caso a conta de origem não seja encontrada.
     * @throws InsufficientBalanceException  Caso a conta não tenha saldo suficiente.
     * @throws InvalidAmountException        Caso o valor da transferência seja inválido.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public GoalResponse depositToGoal(UUID id, GoalTransferRequest transfer, User user){

        log.info("Realizando transação da conta {}, para a Meta {}", transfer.accountId(), id);

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

        return goalMapper.toGoalResponse(saveGoal);
    }

    /**
     * Realiza um resgate da meta, transferindo os fundos de volta para uma conta do usuário.
     * Esta operação gera uma transação de "RECEITA" na conta (dinheiro entrando) descontando do saldo da meta.
     * Limpa o cache de metas.
     *
     * @param id       ID da meta de onde o valor será resgatado.
     * @param transfer DTO contendo o valor, a conta de destino e a chave de idempotência.
     * @param user     Entidade do usuário logado.
     * @return DTO da meta atualizada com o novo saldo reduzido.
     * @throws IdempotencyKeyException       Caso uma transação com a mesma chave de idempotência já tenha sido processada.
     * @throws GoalNotFoundException         Caso a meta não seja encontrada.
     * @throws AccountNotFoundException      Caso a conta de destino não seja encontrada.
     * @throws InsufficientBalanceException  Caso a meta não tenha saldo suficiente para o resgate.
     * @throws InvalidAmountException        Caso o valor do resgate seja inválido.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public GoalResponse withdrawFromGoal(UUID id, GoalTransferRequest transfer, User user){

        log.info("Resgatando valor do Goal {} para a conta {}", id, transfer.accountId());

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
        return goalMapper.toGoalResponse(saveGoal);
    }

    /**
     * Remove permanentemente uma meta da base de dados.
     * A exclusão é bloqueada caso a meta ainda possua saldo acumulado.
     * Limpa o cache de metas.
     *
     * @param id   ID da meta a ser deletada.
     * @param user Entidade do usuário dono da meta.
     * @throws GoalNotFoundException    Caso a meta não seja encontrada.
     * @throws IllegalArgumentException Caso a meta ainda contenha saldo maior que zero.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteGoal(UUID id, User user) {

        log.info("Deletando Meta {}", id);

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(GoalNotFoundException::new);

        if (goal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0){

            throw new IllegalArgumentException(
                    "Não é possível deletar a Meta pois ela contém saldo. " +
                            "Resgate o dinheiro antes.");
        }

        user.removeGoals(goal);
        goalRepository.delete(goal);

        goalMetrics.incrementDelete();

        log.info("Meta deletada com sucesso!");
    }
}
