package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.user.User;
import org.springframework.stereotype.Component;

@Component
public class GoalMapper {

    public Goal toGoal(GoalRequest request, User user, Category category) throws InvalidAmountException {

        if (request == null){
            return null;
        }

        return new Goal(

                user,
                category,
                request.name(),
                request.description(),
                request.targetAmount(),
                request.deadLine(),
                request.priority()
        );
    }

    public GoalResponse toGoalResponse(Goal goal){

        if (goal == null){
            return null;
        }

        return new GoalResponse(

                goal.getCategory().getName(),
                goal.getName(),
                goal.getDescription(),
                goal.getCurrentAmount(),
                goal.getTargetAmount(),
                goal.getPriority(),
                goal.getDeadline()
        );
    }
}
