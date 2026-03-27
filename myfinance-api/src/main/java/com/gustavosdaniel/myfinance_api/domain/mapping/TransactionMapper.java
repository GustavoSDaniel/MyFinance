package com.gustavosdaniel.myfinance_api.domain.mapping;

import com.gustavosdaniel.myfinance_api.domain.dto.request.GoalTransferRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.TransferRequest;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.domain.po.Transaction;
import com.gustavosdaniel.myfinance_api.exception.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.domain.dto.request.TransactionRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.response.TransactionResponse;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import org.springframework.stereotype.Component;


@Component
public class TransactionMapper {

    private final CategoryMapper categoryMapper;

    public TransactionMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public Transaction toTransaction(TransactionRequest request, User user, Account account, Category category) throws InvalidAmountException {

        if (request == null){
            return null;
        }

        return new  Transaction(
                request.idempotencyKey(),
                user,
                account,
                category,
                request.description(),
                request.amount(),
                request.type(),
                request.date().atStartOfDay(),
                request.isRecurring(),
                request.recurrenceType()
        );
    }

    public TransactionResponse toTransactionResponse(Transaction transaction){

        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTime(),
                transaction.getStatus(),
                transaction.getAccount().getId(),
                transaction.getAccount().getName(),
                categoryMapper.toCategoryResponse(transaction.getCategory()),
                transaction.getIsRecurring(),
                transaction.getRecurrenceType()
        );
    }

    /**
     * Converte um objeto {@link GoalTransferRequest} e as entidades relacionadas em uma entidade {@link Transaction}.
     * <p>
     * Utilizado para criar transações de depósito ou resgate em metas (goals). O tipo da transação
     * (RECEITA ou DESPESA) é definido pelo parâmetro {@code type}.
     * </p>
     * <p>
     * A data da transação é convertida para o início do dia ({@code LocalTime.MIN}).
     * </p>
     *
     * @param request  Objeto contendo os dados da transação (valor, descrição, chave de idempotência, etc.)
     * @param user     Entidade {@link User} associada à transação
     * @param account  Entidade {@link Account} associada à transação (conta de origem/destino)
     * @param category Entidade {@link Category} associada à transação
     * @param type     Tipo da transação ({@link TransactionType#RECEITA} ou {@link TransactionType#DESPESA})
     * @return Uma nova instância de {@link Transaction} preenchida
     */
    public Transaction toTransactionGoal(
            GoalTransferRequest request,
            User user, Account account, Category category, TransactionType type){

        return new  Transaction(
                request.idempotencyKey(),
                user,
                account,
                category,
                request.description(),
                request.amount(),
                type,
                request.date().atStartOfDay(),
                request.isRecurring(),
                request.recurrenceType()
        );
    }

    /**
     * Converte um objeto {@link TransferRequest} e as entidades relacionadas em uma entidade {@link Transaction}.
     * <p>
     * Utilizado para criar transações de transferência entre contas. Uma transferência gera duas transações:
     * uma DESPESA na conta de origem e uma RECEITA na conta de destino. Este método é chamado para cada uma delas.
     * </p>
     * <p>
     * A data da transação é convertida para o início do dia ({@code LocalTime.MIN}).
     * </p>
     *
     * @param request  Objeto contendo os dados da transferência (valor, descrição, chave de idempotência, etc.)
     * @param user     Entidade {@link User} associada à transação
     * @param account  Entidade {@link Account} associada à transação (conta de origem ou destino)
     * @param category Entidade {@link Category} associada à transação
     * @param type     Tipo da transação ({@link TransactionType#RECEITA} para a conta destino,
     *                 {@link TransactionType#DESPESA} para a conta origem)
     * @return Uma nova instância de {@link Transaction} preenchida
     */
    public Transaction toTransfer(
            TransferRequest request,
            User user,Account account ,Category category, TransactionType type) {

        return new Transaction(
                request.idempotencyKey(),
                user,
                account,
                category,
                request.description(),
                request.amount(),
                type,
                request.date().atStartOfDay(),
                request.isRecurring(),
                request.recurrenceType()
        );

    }
}
