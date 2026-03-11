package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.accounts.Account;
import com.gustavosdaniel.myfinance_api.accounts.AccountRepository;
import com.gustavosdaniel.myfinance_api.accounts.AccountType;
import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.categories.CategoryRepository;
import com.gustavosdaniel.myfinance_api.categories.CategoryType;
import com.gustavosdaniel.myfinance_api.transactions.Transaction;
import com.gustavosdaniel.myfinance_api.transactions.TransactionRepository;
import com.gustavosdaniel.myfinance_api.transactions.TransactionType;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserRole;
import com.gustavosdaniel.myfinance_api.exception.InsufficientBalanceException;
import com.gustavosdaniel.myfinance_api.util.InvalidAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GoalService goalService;


    @Nested
    class createGoal{

        @Test
        @DisplayName("Should create goal with sucesso")
        void createGoalWithSucesso() throws InvalidAmountException {

            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UUID goalId = UUID.randomUUID();

            LocalDate dateMeta = LocalDate.of(2026, 11, 29);

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            GoalRequest request = new GoalRequest(
                    categoryId,
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta, PriorityStatus.MEDIUM );

            Goal goal = new Goal(
                    user,
                    category,
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );

            GoalResponse response = new GoalResponse(
                    goalId,
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta
            );

            when(goalRepository.existsByNameIgnoreCaseAndUserId(request.name(), userId)).thenReturn(false);
            when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
            when(goalMapper.toGoal(request, user, category)).thenReturn(goal);
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toGoalResponse(goal)).thenReturn(response);

            GoalResponse output = goalService.createGoal(user, request);

            assertNotNull(output);
            assertEquals(response, output);

            verify(goalRepository).existsByNameIgnoreCaseAndUserId(request.name(), userId);
            verify(categoryRepository).findByIdAndUserId(categoryId, userId);
            verify(goalMapper).toGoal(request,user, category);
            verify(goalRepository).save(goal);
            verify(goalMapper).toGoalResponse(goal);

        }
    }

    @Nested
    class getGoalById{

        @Test
        @DisplayName("Should Find Goal by id with sucesso")
        void shouldGoalByIdWithSucesso() throws InvalidAmountException {

            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UUID goalId = UUID.randomUUID();
            LocalDate dateMeta = LocalDate.of(2026, 11, 29);


            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            Goal goal = new Goal(
                    user,
                    category,
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId);

            GoalResponse response = new GoalResponse(
                    goalId,
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
            when(goalMapper.toGoalResponse(goal)).thenReturn(response);

            GoalResponse output = goalService.getGoalById(goalId, user);

            assertNotNull(output);
            assertEquals(response, output);

            verify(goalRepository).findByIdAndUserId(goalId, userId);
            verify(goalMapper).toGoalResponse(goal);
        }
    }

    @Nested
    class searchGoal{

        @Test
        @DisplayName("Should search name goal with sucesso")
        void shouldSearchNameGoalWithSucesso() throws InvalidAmountException {

            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            UUID goalId = UUID.randomUUID();
            UUID goalId2 = UUID.randomUUID();
            UUID goalId3 = UUID.randomUUID();

            String goalName = "Casa nova";
            String goalName2 = "Carro novo";
            String goalName3 = "Salário novo";


            LocalDate dateMeta = LocalDate.of(2026, 11, 29);

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            Goal goal = new Goal(
                    user,
                    category,
                    goalName,
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId);

            Goal goal2 = new Goal(
                    user,
                    category,
                    goalName2,
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId2);

            Goal goal3 = new Goal(
                    user,
                    category,
                    goalName3,
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId3);

            List<Goal> goals = Arrays.asList(goal3, goal2, goal);

            GoalResponse response = new GoalResponse(
                    goalId,
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            GoalResponse response2 = new GoalResponse(
                    goalId2,
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            GoalResponse response3 = new GoalResponse(
                    goalId3,
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            when(goalRepository.searchName(anyString(), eq(userId))).thenReturn(goals);
            when(goalMapper.toGoalResponse(goal)).thenReturn(response);
            when(goalMapper.toGoalResponse(goal2)).thenReturn(response2);
            when(goalMapper.toGoalResponse(goal3)).thenReturn(response3);

            List<GoalResponse> output = goalService.searchGoal(user, goalName);

            assertNotNull(output);
            assertEquals(3, output.size());

            verify(goalRepository).searchName(goalName, userId);
            verify(goalMapper).toGoalResponse(goal);
            verify(goalMapper).toGoalResponse(goal2);
            verify(goalMapper).toGoalResponse(goal3);


        }
    }

    @Nested
    class getAllGoals{

        @Test
        @DisplayName("Should with sucesso all goals")
        void shouldWithSucessoAllGoals() throws InvalidAmountException {

            Pageable pageable = PageRequest.of(0, 10);

            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            UUID goalId = UUID.randomUUID();
            UUID goalId2 = UUID.randomUUID();
            UUID goalId3 = UUID.randomUUID();

            String goalName = "Casa nova";
            String goalName2 = "Carro novo";
            String goalName3 = "Salário novo";

            String status = "para mostrar todos";

            LocalDate dateMeta = LocalDate.of(2026, 11, 29);

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            Goal goal = new Goal(
                    user,
                    category,
                    goalName,
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId);

            Goal goal2 = new Goal(
                    user,
                    category,
                    goalName2,
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId2);

            Goal goal3 = new Goal(
                    user,
                    category,
                    goalName3,
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId3);

            List<Goal> goals = Arrays.asList(goal3, goal2, goal);

            Page<Goal> goalsPage = new PageImpl<>(goals, pageable, goals.size());

            GoalResponse response = new GoalResponse(
                    goalId,
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            GoalResponse response2 = new GoalResponse(
                    goalId2,
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            GoalResponse response3 = new GoalResponse(
                    goalId3,
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            when(goalRepository.findByUserId(userId, pageable)).thenReturn(goalsPage);
            when(goalMapper.toGoalResponse(goal)).thenReturn(response);
            when(goalMapper.toGoalResponse(goal2)).thenReturn(response2);
            when(goalMapper.toGoalResponse(goal3)).thenReturn(response3);

            Page<GoalResponse> output = goalService.getAllGoals(user, status, pageable);

            assertNotNull(output);

            verify(goalRepository).findByUserId(userId, pageable);
            verify(goalMapper).toGoalResponse(goal);
            verify(goalMapper).toGoalResponse(goal2);
            verify(goalMapper).toGoalResponse(goal3);

        }
    }

    @Nested
    class updateGoal{

        @Test
        @DisplayName("Should update Goal with sucesso")
        void shouldUpdateWithSucesso() throws InvalidAmountException {

            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UUID goalId = UUID.randomUUID();

            String goalName = "Casa nova";
            String goalNewName = "Casa nova atualizada";
            String descriptionAtualizado = "Comprar o carro até o final do ano atualizado";

            LocalDate dateMeta = LocalDate.of(2026, 11, 29);
            LocalDate dateMetaAtualizado = LocalDate.of(2028, 3, 6);

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            Goal goal = new Goal(
                    user,
                    category,
                    goalName,
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId);

            GoalRequestUpdate requestUpdate = new GoalRequestUpdate(
                    null,
                    goalNewName,
                    descriptionAtualizado,
                    dateMetaAtualizado,
                    PriorityStatus.LOW);

            GoalResponse response = new GoalResponse(
                    goalId,
                    category.getName(),
                    goalNewName,
                    descriptionAtualizado,
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.LOW,
                    dateMeta);

            when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
            when(goalRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(
                    goalNewName, userId, goalId)).thenReturn(false);
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toGoalResponse(goal)).thenReturn(response);

            GoalResponse output = goalService.updateGoal(goalId, requestUpdate, user);

            assertNotNull(output);
            assertEquals(response, output);

            verify(goalRepository).findByIdAndUserId(goalId, userId);
            verify(goalRepository).existsByNameIgnoreCaseAndUserIdAndIdNot(goalNewName, userId, goalId);
            verify(goalRepository).save(goal);
            verify(goalMapper).toGoalResponse(goal);
        }
    }

    @Nested
    class depositToGoal{

        @Test
        @DisplayName("Should deposit Goal with sucesso")
        void shouldDepositGoalWithSucesso() throws InvalidAmountException, com.gustavosdaniel.myfinance_api.exception.InvalidAmountException, InsufficientBalanceException {

            UUID userId = UUID.randomUUID();
            UUID goalId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            BigDecimal transactionAMount = new BigDecimal("300.96");
            BigDecimal currentBalance = new BigDecimal("500.25");

            String goalName = "Casa nova";

            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 2, 3, 13, 46);
            LocalDate dateMeta = LocalDate.of(2026, 11, 29);

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            Account account = new Account(user, "Poupança", AccountType.POUPANCA, "Fundo de emergencia", null);
            ReflectionTestUtils.setField(account, "id", accountId);
            ReflectionTestUtils.setField(account, "currentBalance", currentBalance);

            Transaction transaction = new Transaction(
                    idempotencyKey,
                    user,
                    account,
                    category,
                    "Para curtir o feriado",
                    transactionAMount,
                    TransactionType.DESPESA,
                    fixedDateTime,
                    false,
                    null);
            ReflectionTestUtils.setField(transaction, "id", transactionId);
            ReflectionTestUtils.setField(transaction, "idempotencyKey", idempotencyKey);

            GoalTransfer transfer = new GoalTransfer(
                    transaction.getIdempotencyKey(),
                    account.getId(),
                    transaction.getAmount(),
                    "Realizando transferencia para conta");

            Goal goal = new Goal(
                    user,
                    category,
                    goalName,
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId);

            GoalResponse response = new GoalResponse(
                    goalId,
                    category.getName(),
                    goalName,
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            when(transactionRepository.existsByIdempotencyKeyAndUserId(idempotencyKey, userId)).thenReturn(false);
            when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(accountRepository.save(any(Account.class))).thenReturn(account);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(goalMapper.toGoalResponse(goal)).thenReturn(response);

            GoalResponse output = goalService.depositToGoal(goalId, transfer, user);

            assertNotNull(output);
            assertEquals(response, output);

            verify(transactionRepository).existsByIdempotencyKeyAndUserId(idempotencyKey, userId);
            verify(goalRepository).findByIdAndUserId(goalId, userId);
            verify(accountRepository).findByIdAndUserId(accountId, userId);
            verify(goalRepository).save(goal);
            verify(accountRepository).save(account);
            verify(transactionRepository).save(any(Transaction.class));
            verify(goalMapper).toGoalResponse(goal);

        }
    }

    @Nested
    class withdrawFromGoal{

        @Test
        @DisplayName("Should draw from Goal with sucesso")
        void shouldDrawGoalWithSucesso() throws InvalidAmountException, com.gustavosdaniel.myfinance_api.exception.InvalidAmountException, InsufficientBalanceException {

            UUID userId = UUID.randomUUID();
            UUID goalId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            BigDecimal transactionAMount = new BigDecimal("300.96");
            BigDecimal currentBalance = new BigDecimal("500.25");
            BigDecimal currentAmount = new BigDecimal("854.86");

            String goalName = "Casa nova";

            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 2, 3, 13, 46);
            LocalDate dateMeta = LocalDate.of(2026, 11, 29);

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Descanso", CategoryType.TRANSFERENCIA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            Account account = new Account(user, "Poupança", AccountType.POUPANCA, "Fundo de emergencia", null);
            ReflectionTestUtils.setField(account, "id", accountId);
            ReflectionTestUtils.setField(account, "currentBalance", currentBalance);

            Transaction transaction = new Transaction(
                    idempotencyKey,
                    user,
                    account,
                    category,
                    "Para curtir o feriado",
                    transactionAMount,
                    TransactionType.RECEITA,
                    fixedDateTime,
                    false,
                    null);
            ReflectionTestUtils.setField(transaction, "id", transactionId);
            ReflectionTestUtils.setField(transaction, "idempotencyKey", idempotencyKey);

            GoalTransfer transfer = new GoalTransfer(
                    transaction.getIdempotencyKey(),
                    account.getId(),
                    transaction.getAmount(),
                    "Realizando transferencia para conta");

            Goal goal = new Goal(
                    user,
                    category,
                    goalName,
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId);
            ReflectionTestUtils.setField(goal, "currentAmount", currentAmount);

            GoalResponse response = new GoalResponse(
                    goalId,
                    category.getName(),
                    goalName,
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            when(transactionRepository.existsByIdempotencyKeyAndUserId(idempotencyKey, userId)).thenReturn(false);
            when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(accountRepository.save(any(Account.class))).thenReturn(account);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(goalMapper.toGoalResponse(goal)).thenReturn(response);

            GoalResponse output = goalService.withdrawFromGoal(goalId, transfer, user);

            assertNotNull(output);
            assertEquals(response, output);

            verify(transactionRepository).existsByIdempotencyKeyAndUserId(idempotencyKey, userId);
            verify(goalRepository).findByIdAndUserId(goalId, userId);
            verify(accountRepository).findByIdAndUserId(accountId, userId);
            verify(goalRepository).save(goal);
            verify(accountRepository).save(account);
            verify(transactionRepository).save(any(Transaction.class));
            verify(goalMapper).toGoalResponse(goal);

        }
    }

    @Nested
    class deleteGoal{

        @Test
        @DisplayName("Delete Goal with sucesso")
        void deleteGoalWithSucesso() throws InvalidAmountException {

            UUID userId = UUID.randomUUID();
            UUID goalId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            String goalName = "Casa nova";

            LocalDate dateMeta = LocalDate.of(2026, 11, 29);

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Category category = new Category(user, "Descanso", CategoryType.TRANSFERENCIA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            Goal goal = new Goal(
                    user,
                    category,
                    goalName,
                    "Comprar o carro até o final do ano",
                    new BigDecimal("60300.86"),
                    dateMeta,
                    PriorityStatus.MEDIUM );
            ReflectionTestUtils.setField(goal, "id", goalId);

            when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));

            goalService.deleteGoal(goalId, user);

            verify(goalRepository).findByIdAndUserId(goalId, userId);


        }
    }

}