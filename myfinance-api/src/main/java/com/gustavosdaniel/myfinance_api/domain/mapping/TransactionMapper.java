package com.gustavosdaniel.myfinance_api.domain.mapping;

import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.domain.po.Transaction;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.domain.dto.request.TransactionRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.response.TransactionResponse;
import org.springframework.stereotype.Component;

/**
 * Componente responsável por mapear (converter) objetos relacionados à entidade {@link Transaction}.
 * <p>
 * Esta classe fornece métodos para converter:
 * <ul>
 *   <li>{@link TransactionRequest} + entidades relacionadas → {@link Transaction} (entidade)</li>
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
     * A data da transação é convertida para o início do dia ( ).
     * </p>
     *
     * @param request  Objeto contendo os dados da transação fornecidos pelo cliente.
     *                 Pode ser {@code null}.
     * @param user     Entidade {@link User} associada à transação (proprietário).
     * @param account  Entidade {@link Account} associada à transação (conta bancária/carteira).
     * @param category Entidade {@link Category} associada à transação (categoria de receita/despesa).
     * @return Uma nova instância de {@link Transaction} preenchida com os dados fornecidos,
     *         ou {@code null} se o {@code request} for {@code null}.
     */
    public Transaction toTransaction(TransactionRequest request, User user, Account account, Category category){

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
     * Converte uma entidade {@link Transaction} em um objeto {@link TransactionResponse}
     * (DTO de resposta).
     * <p>
     * O método extrai os dados da entidade e utiliza o {@link CategoryMapper}
     * para obter a representação
     * da categoria associada. Os campos como ID da conta, nome da conta e
     * informações da categoria são
     * incluídos no DTO.
     * </p>
     *
     * @param transaction Entidade {@link Transaction} a ser convertida. Não deve ser {@code null}.
     * @return Um objeto {@link TransactionResponse} contendo os dados da transação
     * formatados para a resposta da API.
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
}
