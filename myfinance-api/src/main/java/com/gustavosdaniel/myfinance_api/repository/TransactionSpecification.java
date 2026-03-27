package com.gustavosdaniel.myfinance_api.repository;

import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionStatus;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;
import com.gustavosdaniel.myfinance_api.domain.po.Transaction;
import com.gustavosdaniel.myfinance_api.domain.dto.response.TransactionSearchFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe utilitária responsável por fornecer especificações (Specifications) JPA
 * para consultas dinâmicas à entidade {@link Transaction}.
 * <p>
 * As especificações podem ser combinadas utilizando os métodos da interface
 * {@link Specification} para criar consultas complexas de forma programática,
 * aproveitando o recurso {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor}.
 * </p>
 * <p>
 * Esta classe contém métodos estáticos que retornam {@link Specification} para
 * filtrar transações por diversos critérios como usuário, conta, categoria,
 * descrição, tipo, status e intervalo de datas. O método {@link #filters(UUID, TransactionSearchFilter)}
 * combina todas as especificações com base nos filtros fornecidos.
 * </p>
 */
public class TransactionSpecification {

    /**
     * Construtor privado para evitar instanciação da classe utilitária.
     */
    private TransactionSpecification(){}

    /**
     * Cria uma especificação para filtrar transações pelo ID do usuário.
     * Este é um filtro obrigatório para todas as consultas de transações.
     *
     * @param userId Identificador do usuário (UUID). Não pode ser nulo.
     * @return Uma Specification que adiciona a condição {@code user.id = :userId}.
     * @throws IllegalArgumentException Se {@code userId} for {@code null}.
     */
    public static Specification<Transaction> byUserId(UUID userId){

        if (userId == null){

            throw new IllegalArgumentException("ID do usuário é obrigatório para filtrar transações");
        }

        return (root, query, criteriaBuilder) ->

                criteriaBuilder.equal(root.get("user").get("id"), userId);

    }

    /**
     * Cria uma especificação para filtrar transações pelo ID da conta.
     *
     * @param accountId Identificador da conta (UUID). Se for {@code null}, retorna {@code null}.
     * @return Uma Specification que adiciona a condição {@code account.id = :accountId},
     *         ou {@code null} se o parâmetro for {@code null}.
     */
    public static Specification<Transaction> byAccount(UUID accountId){

        if (accountId == null){
            return null;
        }

        return (root, query, criteriaBuilder) ->

                criteriaBuilder.equal(root.get("account").get("id"), accountId);

    }

    /**
     * Cria uma especificação para filtrar transações pelo ID da categoria.
     *
     * @param categoryId Identificador da categoria (UUID). Se for {@code null}, retorna {@code null}.
     * @return Uma Specification que adiciona a condição {@code category.id = :categoryId},
     *         ou {@code null} se o parâmetro for {@code null}.
     */
    public static Specification<Transaction> byCategoryId(UUID categoryId){

        if (categoryId == null){
            return null;
        }

        return (root, query, criteriaBuilder) ->

                criteriaBuilder.equal(root.get("category").get("id"), categoryId);

    }

    /**
     * Cria uma especificação para filtrar transações cuja descrição contenha o texto fornecido
     * (busca case-insensitive, ignorando espaços extras).
     *
     * @param description Texto a ser buscado na descrição da transação. Se for {@code null}, retorna {@code null}.
     * @return Uma Specification que adiciona a condição {@code LOWER(description) LIKE %:desc%},
     *         ou {@code null} se o parâmetro for {@code null}.
     */
    public static Specification<Transaction> byDescription(String description){

        if (description == null){
            return null;
        }

        return (root, query, criteriaBuilder) ->

                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + description.toLowerCase().trim() + "%"
                );


    }

    /**
     * Cria uma especificação para filtrar transações pelo tipo (RECEITA ou DESPESA).
     *
     * @param type Tipo da transação ({@link TransactionType}). Se for {@code null}, retorna {@code null}.
     * @return Uma Specification que adiciona a condição {@code type = :type},
     *         ou {@code null} se o parâmetro for {@code null}.
     */
    public static Specification<Transaction> byTransactionType(TransactionType type){

        if (type == null){
            return null;
        }

        return (root, query, criteriaBuilder) ->

                criteriaBuilder.equal(root.get("type"),type);

    }

    /**
     * Cria uma especificação para filtrar transações pelo status.
     *
     * @param status Status da transação ({@link TransactionStatus}). Se for {@code null}, retorna {@code null}.
     * @return Uma Specification que adiciona a condição {@code status = :status},
     *         ou {@code null} se o parâmetro for {@code null}.
     */
    public static Specification<Transaction> byTransactionStatus(TransactionStatus status){

        if (status == null){
            return null;
        }

        return (root, query, criteriaBuilder) ->

                criteriaBuilder.equal(root.get("status"), status);

    }

    /**
     * Cria uma especificação para filtrar transações por intervalo de datas (campo {@code time}).
     * <p>
     * O intervalo é tratado de forma inclusiva em ambas as extremidades:
     * <ul>
     *   <li>Se apenas {@code startDate} for fornecido: {@code time >= startDate}</li>
     *   <li>Se apenas {@code endDate} for fornecido: {@code time <= endDate}</li>
     *   <li>Se ambos forem fornecidos: {@code time BETWEEN startDate AND endDate}</li>
     *   <li>Se ambos forem nulos: retorna {@code null}</li>
     * </ul>
     * </p>
     *
     * @param startDate Data/hora inicial (pode ser {@code null}, inclusive).
     * @param endDate   Data/hora final (pode ser {@code null}, inclusive).
     * @return Uma Specification com a condição de data apropriada,
     *         ou {@code null} se ambos os parâmetros forem {@code null}.
     */
    public static Specification<Transaction> byDateRange (LocalDateTime startDate, LocalDateTime endDate) {

        if (startDate == null && endDate == null){
            return null;
        }

        return (root, query, criteriaBuilder) -> {

            if (startDate != null && endDate == null){

                return criteriaBuilder.greaterThanOrEqualTo(root.get("time"), startDate);
            }

            if (startDate == null && endDate != null){

                return criteriaBuilder.lessThanOrEqualTo(root.get("time"), endDate);
            }

            return criteriaBuilder.between(root.get("time"), startDate, endDate);

        };
    }

    /**
     * Combina todas as especificações disponíveis com base nos dados fornecidos por
     * {@link TransactionSearchFilter} e pelo ID do usuário.
     * <p>
     * O método inicia com a especificação obrigatória {@link #byUserId(UUID)} e,
     * para cada campo presente no filtro (não nulo), adiciona a respectiva especificação
     * utilizando o operador {@code AND}. Todos os filtros são combinados na mesma query.
     * </p>
     *
     * @param userId Identificador do usuário (obrigatório, não nulo). Será repassado a {@link #byUserId(UUID)}.
     * @param filter Objeto contendo os critérios de filtragem opcionais (conta, categoria, descrição, etc.).
     * @return Uma {@link Specification} composta com todas as condições aplicáveis.
     * @throws IllegalArgumentException Se {@code userId} for {@code null} (propagada do método {@code byUserId}).
     */
    public static Specification<Transaction> filters(UUID userId, TransactionSearchFilter filter){

        Specification<Transaction> specification = Specification.where(byUserId(userId));

        if (filter.accountId() != null){

            specification.and(byAccount(filter.accountId()));
        }

        if (filter.categoryId() != null){
            specification.and(byCategoryId(filter.categoryId()));
        }

        if (filter.description() != null){
            specification.and(byDescription(filter.description()));
        }

        if (filter.type() != null){

            specification.and(byTransactionType(filter.type()));
        }

        if (filter.status() != null){

            specification.and(byTransactionStatus(filter.status()));
        }

        if (filter.startDate() != null || filter.endDate() != null ){

            specification.and(byDateRange(filter.startDate(), filter.endDate()));
        }

        return specification;

    }
}
