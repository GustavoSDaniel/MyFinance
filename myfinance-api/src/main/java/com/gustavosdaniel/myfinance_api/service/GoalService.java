package com.gustavosdaniel.myfinance_api.service;

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
import com.gustavosdaniel.myfinance_api.user.User;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    public GoalService(GoalRepository goalRepository, GoalMapper goalMapper, CategoryRepository categoryRepository, AccountRepository accountRepository, TransactionRepository transactionRepository, AuthHelper authHelper) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.authHelper = authHelper;
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<GoalResponse> createGoal(OAuth2User principal, GoalRequest request){

        User user = authHelper.getCurrentUser(principal);

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

        return ResponseEntity.created(uri).body(goalMapper.toGoalResponse(saveGoal));
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "{#id, #user.id}")
    public ResponseEntity<GoalResponse> getGoalById(UUID id, OAuth2User principal) {

        log.info("Buscando Meta pelo id {}", id);

        User user = authHelper.getCurrentUser(principal);

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(GoalNotFoundException::new);

        log.info("Meta {} encontrada com sucesso", goal.getName());

        return ResponseEntity.ok(goalMapper.toGoalResponse(goal));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<GoalResponse>> searchGoal(OAuth2User principal, String name) {

        log.info("Buscando Metas pelo nome {}", name);

        User user = authHelper.getCurrentUser(principal);

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

    @Transactional(readOnly = true)
    public ResponseEntity<Page<GoalResponse>> getAllGoals(
            OAuth2User principal, String status,  Pageable pageable) {

        User user = authHelper.getCurrentUser(principal);

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

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<GoalResponse> updateGoal(
            UUID id, GoalRequestUpdate requestUpdate, OAuth2User principal) {

        User user = authHelper.getCurrentUser(principal);

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

        return ResponseEntity.ok(goalMapper.toGoalResponse(saveGoal));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<GoalResponse> depositToGoal(
            UUID id, GoalTransfer transfer, OAuth2User principal) {

        log.info("Realizando transação da conta {}, para a Meta {}", transfer.accountId(), id);

        User user = authHelper.getCurrentUser(principal);

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

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<GoalResponse> withdrawFromGoal(
            UUID id, GoalTransfer transfer, OAuth2User principal) {

        log.info("Resgatando valor do Goal {} para a conta {}", id, transfer.accountId());

        User user = authHelper.getCurrentUser(principal);

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

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deleteGoal(UUID id, OAuth2User principal) {

        log.info("Deletando Meta {}", id);

        User user = authHelper.getCurrentUser(principal);

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        if (goal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0){

            throw new IllegalArgumentException(
                    "Não é possível deletar a Meta pois ela contém saldo. " +
                            "Resgate o dinheiro antes.");
        }

        user.removeGoals(goal);
        goalRepository.delete(goal);

        log.info("Meta deletada com sucesso!");

        return ResponseEntity.noContent().build();
    }
}
