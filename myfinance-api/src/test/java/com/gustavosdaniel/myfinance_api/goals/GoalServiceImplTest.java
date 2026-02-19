package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.accounts.AccountRepository;
import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.categories.CategoryRepository;
import com.gustavosdaniel.myfinance_api.categories.CategoryType;
import com.gustavosdaniel.myfinance_api.transactions.TransactionRepository;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private GoalServiceImpl goalService;


    @Nested
    class createGoal{

        @Test
        @DisplayName("Should create goal with sucesso")
        void createGoalWithSucesso() throws InvalidAmountException {

            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

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
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

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
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            GoalResponse response2 = new GoalResponse(
                    category.getName(),
                    "Carro novo",
                    "Comprar o carro até o final do ano",
                    BigDecimal.ZERO,
                    new BigDecimal("60300.86"),
                    PriorityStatus.MEDIUM,
                    dateMeta);

            GoalResponse response3 = new GoalResponse(
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



}