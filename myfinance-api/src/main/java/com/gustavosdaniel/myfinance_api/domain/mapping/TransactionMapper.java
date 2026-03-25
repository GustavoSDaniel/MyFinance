package com.gustavosdaniel.myfinance_api.domain.mapping;

import com.gustavosdaniel.myfinance_api.domain.dto.request.GoalTransferRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.TransferRequest;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.domain.po.Transaction;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.domain.dto.request.TransactionRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.response.TransactionResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Componente responsável por mapear (converter) objetos relacionados à entidade {@link Transaction}.
 * <p>
 * Esta classe fornece métodos para converter:
 * <ul>
 *   <li>DTOs de requisição ({@link TransactionRequest}, {@link GoalTransferRequest}, {@link TransferRequest}) + entidades relacionadas → {@link Transaction} (entidade)</li>
 *   <li>{@link Transaction} → {@link TransactionResponse} (DTO de resposta)</li>
 * </ul>
 * Utiliza {@link CategoryMapper} para conversão da categoria associada.
 * </p>
 */
@Component
public class TransactionMapper {

    private final CategoryMapper categoryMapper;

    public TransactionMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /**
     * Converte um objeto {@link TransactionRequest} e as entidades relacionadas em uma entidade {@link Transaction}.
     * <p>
     * O método valida se o objeto de requisição é nulo e, caso seja, retorna {@code null}.
     * A data da transação é convertida para o início do dia ({@code LocalTime.MIN}).
     * </p>
     *
     * @param request  Objeto contendo os dados da transação fornecidos pelo cliente. Pode ser {@code null}.
     * @param user     Entidade {@link User} associada à transação (proprietário).
     * @param account  Entidade {@link Account} associada à transação (conta bancária/carteira).
     * @param category Entidade {@link Category} associada à transação (categoria de receita/despesa).
     * @return Uma nova instância de {@link Transaction} preenchida com os dados fornecidos,
     *         ou {@code null} se o {@code request} for {@code null}.
     */
    public Transaction toTransaction(
            TransactionRequest request, User user, Account account, Category category){

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

    /**
     * Converte uma entidade {@link Transaction} em um objeto {@link TransactionResponse} (DTO de resposta).
     * <p>
     * O método extrai os dados da entidade e utiliza o {@link CategoryMapper} para obter a representação
     * da categoria associada. Os campos como ID da conta, nome da conta e informações da categoria são
     * incluídos no DTO.
     * </p>
     *
     * @param transaction Entidade {@link Transaction} a ser convertida. Não deve ser {@code null}.
     * @return Um objeto {@link TransactionResponse} contendo os dados da transação formatados para a resposta da API.
     */
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
