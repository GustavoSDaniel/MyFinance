package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GoalService {

     GoalResponse createGoal(User user, GoalRequest request) throws InvalidAmountException;

     GoalResponse getGoalById(UUID id, User user);

     Page<GoalResponse> getAllGoals(User user, String status,  Pageable pageable);
}
