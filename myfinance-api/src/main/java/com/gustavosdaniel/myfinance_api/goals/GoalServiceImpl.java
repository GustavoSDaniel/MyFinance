package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.exception.AccountNotFoundException;
import com.gustavosdaniel.myfinance_api.repository.AccountRepository;
import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.exception.CategoryNotFoundException;
import com.gustavosdaniel.myfinance_api.repository.CategoryRepository;
import com.gustavosdaniel.myfinance_api.exception.GoalNameDuplicateException;
import com.gustavosdaniel.myfinance_api.exception.GoalNotFoundException;
import com.gustavosdaniel.myfinance_api.exception.IdempotencyKeyException;
import com.gustavosdaniel.myfinance_api.repository.GoalRepository;
import com.gustavosdaniel.myfinance_api.transactions.Transaction;
import com.gustavosdaniel.myfinance_api.repository.TransactionRepository;
import com.gustavosdaniel.myfinance_api.transactions.TransactionType;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.exception.InsufficientBalanceException;
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

@Service
@CacheConfig(cacheNames = "goals")
public class GoalServiceImpl implements GoalService{

    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Logger log = LoggerFactory.getLogger(GoalServiceImpl.class);

    public GoalServiceImpl(GoalRepository goalRepository, GoalMapper goalMapper, CategoryRepository categoryRepository, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    @Override
    @CacheEvict(allEntries = true)
    public GoalResponse createGoal(User user, GoalRequest request) throws InvalidAmountException {

        log.info("Criando Meta para o usuário: {}", user.getName());

        if (goalRepository.existsByNameIgnoreCaseAndUserId(request.name().trim(), user.getId())){

            throw new GoalNameDuplicateException();
        }

        Category category = categoryRepository
                .findByIdAndUserId(request.categoryId(), user.getId()).orElseThrow(CategoryNotFoundException::new);

        Goal newGoal = goalMapper.toGoal(request, user, category);
        category.addGoal(newGoal);

        Goal saveGoal = goalRepository.save(newGoal);

        log.info("Meta criado com sucesso: {}", saveGoal.getName());

        return goalMapper.toGoalResponse(saveGoal);
    }

    @Transactional(readOnly = true)
    @Override
    @Cacheable(key = "{#id, #user.id}")
    public GoalResponse getGoalById(UUID id, User user) {

        log.info("Buscando Meta pelo id {}", id);

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        log.info("Meta {} encontrada com sucesso", goal.getName());

        return goalMapper.toGoalResponse(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> searchGoal(User user, String name) {

        log.info("Buscando Metas pelo nome {}", name);

        List<Goal> goals = goalRepository.searchName(name, user.getId());

        if (goals.isEmpty()){

            log.warn("Nenhuma Meta desse usuário {}  encontrado com esse nome {}", user.getName(), name);

            return List.of();
        }

        log.info("Total de Metas encontrados {}", goals.size());

        return goals.stream().map(goalMapper::toGoalResponse).toList();
    }

    @Override
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

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public GoalResponse updateGoal(UUID id, GoalRequestUpdate requestUpdate, User user) {

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

        return goalMapper.toGoalResponse(saveGoal);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public GoalResponse depositToGoal(UUID id, GoalTransfer transfer, User user) throws com.gustavosdaniel.myfinance_api.exception.InvalidAmountException, InsufficientBalanceException, InvalidAmountException {

        log.info("Realizando transação da conta {}, para a Meta {}", transfer.accountId(), id);

        if (transactionRepository.existsByIdempotencyKeyAndUserId(transfer.idempotencyKey(), user.getId())){

            log.warn("Transação já processada anteriormente. idempotencyKey = {}", transfer.idempotencyKey());

            throw new IdempotencyKeyException();
        }

        Goal goal = goalRepository
                .findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        Account account = accountRepository
                .findByIdAndUserId(transfer.accountId(), user.getId()).orElseThrow(AccountNotFoundException::new);

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

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public GoalResponse withdrawFromGoal(UUID id, GoalTransfer transfer, User user) throws com.gustavosdaniel.myfinance_api.exception.InvalidAmountException, InsufficientBalanceException, InvalidAmountException {

        log.info("Resgatando valor do Goal {} para a conta {}", id, transfer.accountId());

        if (transactionRepository.existsByIdempotencyKeyAndUserId(transfer.idempotencyKey(), user.getId())){

            log.warn("Transação já processada anteriormente. idempotencyKey = {}", transfer.idempotencyKey());

            throw new IdempotencyKeyException();
        }

        Goal goal = goalRepository
                .findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        Account account = accountRepository
                .findByIdAndUserId(transfer.accountId(), user.getId()).orElseThrow(AccountNotFoundException::new);

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

        log.info("Resgate no valor de {} realizado com sucesso para a conta {}", transfer.amount(), account.getName());
        return goalMapper.toGoalResponse(saveGoal);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteGoal(UUID id, User user) {

        log.info("Deletando Meta {}", id);

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        if (goal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0){

            throw new IllegalArgumentException(
                    "Não é possível deletar a Meta pois ela contém saldo. Resgate o dinheiro antes.");
        }

        user.removeGoals(goal);
        goalRepository.delete(goal);

        log.info("Meta deletada com sucesso!");
    }
}
