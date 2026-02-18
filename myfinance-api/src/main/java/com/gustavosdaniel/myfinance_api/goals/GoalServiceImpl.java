package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.accounts.Account;
import com.gustavosdaniel.myfinance_api.accounts.AccountNotFoundException;
import com.gustavosdaniel.myfinance_api.accounts.AccountRepository;
import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.categories.CategoryNotFoundException;
import com.gustavosdaniel.myfinance_api.categories.CategoryRepository;
import com.gustavosdaniel.myfinance_api.transactions.IdempotencyKeyException;
import com.gustavosdaniel.myfinance_api.transactions.Transaction;
import com.gustavosdaniel.myfinance_api.transactions.TransactionRepository;
import com.gustavosdaniel.myfinance_api.transactions.TransactionType;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.InsufficientBalanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
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
    public GoalResponse createGoal(User user, GoalRequest request) throws InvalidAmountException {

        log.info("Criando Goal para o usuário: {}", user.getName());

        if (goalRepository.existsByNameIgnoreCaseAndUserId(request.name().trim(), user.getId())){

            throw new GoalNameDuplicateException();
        }

        Category category = categoryRepository
                .findByIdAndUserId(request.categoryId(), user.getId()).orElseThrow(CategoryNotFoundException::new);

        Goal newGoal = goalMapper.toGoal(request, user, category);
        category.addGoal(newGoal);

        Goal saveGoal = goalRepository.save(newGoal);

        log.info("Goal criado com sucesso: {}", saveGoal.getName());

        return goalMapper.toGoalResponse(saveGoal);
    }

    @Transactional(readOnly = true)
    @Override
    public GoalResponse getGoalById(UUID id, User user) {

        log.info("Buscando Goal pelo id {}", id);

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        log.info("Goal {} encontrada com sucesso", goal.getName());

        return goalMapper.toGoalResponse(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> searchGoal(User user, String name) {

        log.info("Buscando Goals pelo nome {}", name);

        List<Goal> goals = goalRepository.searchName(name, user.getId());

        if (goals.isEmpty()){

            log.warn("Nenhum Goal desse usuário {}  encontrado com esse nome {}", user.getName(), name);

            return List.of();
        }

        log.info("Total de Goals encontrados {}", goals.size());

        return goals.stream().map(goalMapper::toGoalResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GoalResponse> getAllGoals(User user, String status,  Pageable pageable) {

        Page<Goal> goals;

        if ("achieved".equalsIgnoreCase(status)){

            goals = goalRepository.findAchievedGoals(user.getId(), pageable);

            log.info("Todos os Goals já alcançados {}", goals.getTotalElements());

        } else if ("progress".equalsIgnoreCase(status)) {

            goals = goalRepository.findPendingGoals(user.getId(), pageable);

            log.info("Todos os Goals em progresso {}", goals.getTotalElements());


        } else {
            goals = goalRepository.findByUserId(user.getId(), pageable);

            log.info("Todos os Goals encontrados {}", goals.getTotalElements());
        }

        if (goals.isEmpty()){

            log.info("Nenhum Goal foi encontrado");

            return Page.empty();
        }

        return goals.map(goalMapper::toGoalResponse);
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(UUID id, GoalRequestUpdate requestUpdate, User user) {

        log.info("Atualizando Goal {}, do usuário {}", id, user.getName());

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

        log.info("Goal atualizado com sucesso {}", saveGoal.getName());

        return goalMapper.toGoalResponse(saveGoal);
    }

    @Override
    @Transactional
    public GoalResponse depositToGoal(UUID id, GoalTransfer transfer, User user) throws com.gustavosdaniel.myfinance_api.accounts.InvalidAmountException, InsufficientBalanceException, InvalidAmountException {

        log.info("Realizando transação da conta {}, para o Goal {}", transfer.accountId(), id);

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

        log.info("Transação para o Goal {} realizada com sucesso", goal.getName());

        return goalMapper.toGoalResponse(saveGoal);
    }

    @Override
    @Transactional
    public GoalResponse withdrawFromGoal(UUID id, GoalTransfer transfer, User user) throws com.gustavosdaniel.myfinance_api.accounts.InvalidAmountException, InsufficientBalanceException, InvalidAmountException {

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

        log.info("Resgate de {} realizado com sucesso para a conta {}", transfer.amount(), account.getName());
        return goalMapper.toGoalResponse(saveGoal);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID id, User user) {

        log.info("Deletando Goal {}", id);

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId()).orElseThrow(GoalNotFoundException::new);

        if (goal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0){

            throw new IllegalArgumentException(
                    "Não é possível deletar a Goal pois ela contém saldo. Resgate o dinheiro antes.");
        }

        user.removeGoals(goal);
        goalRepository.delete(goal);

        log.info("Goal deletada com sucesso!");
    }

}
