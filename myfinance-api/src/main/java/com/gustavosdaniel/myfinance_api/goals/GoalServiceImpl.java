package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.categories.CategoryNotFoundException;
import com.gustavosdaniel.myfinance_api.categories.CategoryRepository;
import com.gustavosdaniel.myfinance_api.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GoalServiceImpl implements GoalService{

    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final CategoryRepository categoryRepository;
    private final Logger log = LoggerFactory.getLogger(GoalServiceImpl.class);

    public GoalServiceImpl(GoalRepository goalRepository, GoalMapper goalMapper, CategoryRepository categoryRepository) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    @Override
    public GoalResponse createGoal(User user, GoalRequest request) throws InvalidAmountException {

        log.info("Criando Goal para o usuário: {}", user.getName());

        if (goalRepository.existsByNameIgnoreCaseAndUserId(request.name().trim(), user.getId())){

            throw new GoalNameDuplicateException();
        }

        Category category = categoryRepository
                .findByIdAndUserId(user.getId(), request.categoryId()).orElseThrow(CategoryNotFoundException::new);

        Goal newGoal = goalMapper.toGoal(request, user, category);
        user.addGoals(newGoal);
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


}
