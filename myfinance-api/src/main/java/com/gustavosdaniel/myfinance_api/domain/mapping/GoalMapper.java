package com.gustavosdaniel.myfinance_api.domain.mapping;

import com.gustavosdaniel.myfinance_api.domain.dto.GoalRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalResponse;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.domain.po.Goal;
import com.gustavosdaniel.myfinance_api.domain.dto.GoalRequestUpdate;
import com.gustavosdaniel.myfinance_api.util.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.domain.po.User;
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

                goal.getId(),
                goal.getCategory().getName(),
                goal.getName(),
                goal.getDescription(),
                goal.getCurrentAmount(),
                goal.getTargetAmount(),
                goal.getPriority(),
                goal.getDeadline()
        );
    }

    public void toGoalUpdate(GoalRequestUpdate requestUpdate, Goal goal, Category category){

        if (category != null){

            goal.setCategory(category);
        }

        if (requestUpdate.name() != null && !requestUpdate.name().isBlank()){

            goal.setName(requestUpdate.name().trim());
        }

        if (requestUpdate.description() != null){

            goal.setDescription(requestUpdate.description().trim());
        }

        if (requestUpdate.deadLine() != null){

            goal.setDeadline(requestUpdate.deadLine());
        }

        if (requestUpdate.priority() != null){

            goal.setPriority(requestUpdate.priority());
        }
    }
}
