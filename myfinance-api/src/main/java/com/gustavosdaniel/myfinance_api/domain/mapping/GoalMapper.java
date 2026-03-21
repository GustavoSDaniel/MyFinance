package com.gustavosdaniel.myfinance_api.domain.mapping;

import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.domain.po.Goal;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.domain.dto.request.GoalRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.GoalRequestUpdate;
import com.gustavosdaniel.myfinance_api.domain.dto.response.GoalResponse;
import com.gustavosdaniel.myfinance_api.util.InvalidAmountException;
import org.springframework.stereotype.Component;

/**
 * Componente responsável pelo mapeamento e conversão de objetos relacionados à entidade {@link Goal} (Meta).
 */
@Component
public class GoalMapper {

    /**
     * Converte um objeto de requisição, um usuário e uma categoria em uma nova entidade {@link Goal}.
     *
     * @param request  os dados de criação da meta
     * @param user     o usuário proprietário da meta
     * @param category a categoria associada à meta
     * @return uma nova instância de {@link Goal}, ou {@code null} se a requisição for nula
     * @throws InvalidAmountException se o valor alvo (targetAmount) fornecido na requisição for inválido
     */
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

    /**
     * Converte uma entidade {@link Goal} em um DTO {@link GoalResponse}.
     *
     * @param goal a entidade de meta a ser convertida
     * @return uma nova instância de {@link GoalResponse}, ou {@code null} se a meta for nula
     */
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

    /**
     * Atualiza os dados de uma entidade {@link Goal} existente com base nas informações
     * fornecidas em um {@link GoalRequestUpdate} e uma {@link Category} opcional.
     *
     * <p>Apenas os campos que não são nulos (e não estão em branco, aplicável a textos)
     * no objeto de requisição serão atualizados na entidade.
     *
     * @param requestUpdate o objeto contendo os novos dados da meta
     * @param goal          a entidade de meta que será atualizada
     * @param category      a nova categoria a ser associada à meta (caso seja fornecida)
     */
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
