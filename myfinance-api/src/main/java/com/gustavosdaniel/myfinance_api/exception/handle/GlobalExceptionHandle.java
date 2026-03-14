package com.gustavosdaniel.myfinance_api.exception.handle;

import com.gustavosdaniel.myfinance_api.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandle {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandle.class);

    private ProblemDetail buildProblem(HttpStatus status, ProblemType type, String detail){

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);

        problem.setType(type.getUri());
        problem.setTitle(type.getTitle());
        problem.setProperty("timestamp", LocalDateTime.now());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){

        log.warn("Validação falhou {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        ProblemDetail problem = buildProblem(
                HttpStatus.BAD_REQUEST,
                ProblemType.VALIDATE_ERROR,
                "Erro de validação nos campos"
        );

        problem.setProperty("timestamp", LocalDateTime.now());

        return problem;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorizedException(UnauthorizedException ex){

        log.warn("Usuário não autorizado{}", ex.getMessage());

        return buildProblem(HttpStatus.FORBIDDEN,
                ProblemType.UNAUTHORIZED,
                "O usuário não foi autorizado a realizar essa ação");
    }

    //USER
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex){

        log.warn("Usuário não encontrado {}", ex.getMessage());

        return buildProblem(
                HttpStatus.BAD_REQUEST,
                ProblemType.USER_NOT_FOUND,
                "Não foi possivel encontrar o usuário pesquisado"
        );

    }

    // ACCOUNT
    @ExceptionHandler(AccountNameDuplicateException.class)
    public ProblemDetail handleAccountNameDuplicate(AccountNameDuplicateException ex){

        log.warn("Conta com esse nome já existe {}", ex.getMessage());

        return buildProblem(
                HttpStatus.CONFLICT,
                ProblemType.ACCOUNT_NAME_DUPLICATE,
                "Já existe uma conta com esse nome em uso"
        );
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFoundException(AccountNotFoundException ex){

        log.warn("Conta não encontrada {}", ex.getMessage());

        return buildProblem(
                HttpStatus.NOT_FOUND,
                ProblemType.ACCOUNT_NOT_FOUND,
                "A conta pesquisada não foi encontrada"
        );
    }

    // CATEGORY
    @ExceptionHandler(CategoryNameDuplicateException.class)
    public ProblemDetail handleCategoryNameDuplicateException(CategoryNameDuplicateException ex){

        log.warn("Categoria com nome já em uso: {}", ex.getMessage());

        return buildProblem(
                HttpStatus.CONFLICT,
                ProblemType.CATEGORY_NAME_DUPLICATE,
                "Categoria com nome já em uso"
        );
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ProblemDetail handleCategoryNotFoundException(CategoryNotFoundException ex){

        log.warn("Categoria não encontrada: {}", ex.getMessage());

        return buildProblem(
                HttpStatus.NOT_FOUND,
                ProblemType.CATEGORY_NOT_FOUND,
                "A categoria pesquisada não foi encontrada"
        );
    }

    // TRANSACTION
    @ExceptionHandler(TransactionNotFoundException.class)
    public ProblemDetail handleTransactionNotFoundException(TransactionNotFoundException ex){

        log.warn("Transação não encontra: {}", ex.getMessage());

        return buildProblem(
                HttpStatus.NOT_FOUND,
                ProblemType.TRANSACTION_NOT_FOUND,
                "A transação pesquisada não foi encontrada"
        );
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRuleException(BusinessRuleException ex){

        log.warn("Não é possivel apagar transações CONFIRMADAS {}", ex.getMessage());

        return buildProblem(
                HttpStatus.BAD_REQUEST,
                ProblemType.BUSINESS_RULE,
                "Erro ao tentar deletar transação"
        );
    }

    @ExceptionHandler(TransactionEqualsAccountException.class)
    public ProblemDetail handleTransactionEqualsAccountException(TransactionEqualsAccountException ex){

        log.warn("Não é possivel fazer tranferencia para a mesma conta {}", ex.getMessage());

        return buildProblem(
                HttpStatus.BAD_REQUEST,
                ProblemType.TRANSACTION_EQUALS_ACCOUNT,
                "Não é possivel fazer transferencia para a mesma conta"
        );
    }

    @ExceptionHandler(IdempotencyKeyException.class)
    public ProblemDetail handleIdempotencyKeyException(IdempotencyKeyException ex){

        log.warn("Transação já realizada {}", ex.getMessage());;

        return buildProblem(
                HttpStatus.BAD_REQUEST,
                ProblemType.IDEMPOTENCY_KEY,
                "A transação já foi realizada"
        );
    }

    // GOAL
    @ExceptionHandler(GoalNameDuplicateException.class)
    public ProblemDetail handleGoalNameDuplicateException(GoalNameDuplicateException ex){

        log.warn("Nome desse Goal já em uso {}", ex.getMessage());

        return buildProblem(
                HttpStatus.CONFLICT,
                ProblemType.GOAL_NAME_DUPLICATE,
                "O nome já está em uso em outra meta"
        );
    }

    @ExceptionHandler(GoalNotFoundException.class)
    public ProblemDetail handleGoalNotFoundException(GoalNotFoundException ex){

        log.warn("Goal não encontrado {}", ex.getMessage());

        return buildProblem(
                HttpStatus.NOT_FOUND,
                ProblemType.GOAL_NOT_FOUND,
                "A meta buscada não foi encontrada"
        );
    }
}
