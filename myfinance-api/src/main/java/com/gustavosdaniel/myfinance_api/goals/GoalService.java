package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.InsufficientBalanceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GoalService {

     GoalResponse createGoal(User user, GoalRequest request) throws InvalidAmountException;

     GoalResponse getGoalById(UUID id, User user);

     List<GoalResponse> searchGoal(User user, String name);

     Page<GoalResponse> getAllGoals(User user, String status,  Pageable pageable);

     GoalResponse updateGoal(UUID id, GoalRequestUpdate requestUpdate, User user);

     GoalResponse depositToGoal(UUID id, GoalTransfer transfer, User user) throws com.gustavosdaniel.myfinance_api.exception.InvalidAmountException, InsufficientBalanceException, InvalidAmountException;

     GoalResponse withdrawFromGoal(UUID id, GoalTransfer transfer, User user) throws com.gustavosdaniel.myfinance_api.exception.InvalidAmountException, InsufficientBalanceException, InvalidAmountException;

     void deleteGoal(UUID id, User user);
}
