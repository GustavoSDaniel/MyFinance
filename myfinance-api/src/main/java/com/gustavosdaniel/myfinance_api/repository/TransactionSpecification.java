package com.gustavosdaniel.myfinance_api.repository;

import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionStatus;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;
import com.gustavosdaniel.myfinance_api.domain.po.Transaction;
import com.gustavosdaniel.myfinance_api.domain.dto.TransactionSearchFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionSpecification {

    public static Specification<Transaction> byUserId(UUID userId){

        if (userId == null){

            throw new IllegalArgumentException("ID do usuário é obrigatório para filtrar transações");
        }

        return (root, query, criteriaBuilder) ->

             criteriaBuilder.equal(root.get("user").get("id"), userId);

    }

    public static Specification<Transaction> byAccount(UUID accountId){

        if (accountId == null){
            return null;
        }

        return (root, query, criteriaBuilder) ->

             criteriaBuilder.equal(root.get("account").get("id"), accountId);

    }

    public static Specification<Transaction> byCategoryId(UUID categoryId){

        if (categoryId == null){
            return null;
        }

        return (root, query, criteriaBuilder) ->

                criteriaBuilder.equal(root.get("category").get("id"), categoryId);

    }

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

    public static Specification<Transaction> byTransactionType(TransactionType type){

        if (type == null){
            return null;
        }

        return (root, query, criteriaBuilder) ->

             criteriaBuilder.equal(root.get("type"),type);

    }

    public static Specification<Transaction> byTransactionStatus(TransactionStatus status){

        if (status == null){
            return null;
        }

        return (root, query, criteriaBuilder) ->

             criteriaBuilder.equal(root.get("status"), status);

    }


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
