package com.gustavosdaniel.myfinance_api.exception;

import com.gustavosdaniel.myfinance_api.accounts.AccountNameDuplicate;
import com.gustavosdaniel.myfinance_api.accounts.AccountNotFoundException;
import com.gustavosdaniel.myfinance_api.categories.CategoryNameDuplicateException;
import com.gustavosdaniel.myfinance_api.categories.CategoryNotFoundException;
import com.gustavosdaniel.myfinance_api.goals.GoalNameDuplicateException;
import com.gustavosdaniel.myfinance_api.goals.GoalNotFoundException;
import com.gustavosdaniel.myfinance_api.transactions.BusinessRuleException;
import com.gustavosdaniel.myfinance_api.transactions.IdempotencyKeyException;
import com.gustavosdaniel.myfinance_api.transactions.TransactionEqualsAccountException;
import com.gustavosdaniel.myfinance_api.transactions.TransactionNotFoundException;
import com.gustavosdaniel.myfinance_api.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandle {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandle.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){

        log.warn("Validação falhou {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        ErrorResponse erros = new ErrorResponse(
                "Validação falhou",
                "Erro de validação nos campos",
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex){

        log.warn("Usuário não autorizado{}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Usuário não autorizado",
                "O usuário não foi autorizado a realizar essa ação",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    //USER

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex){

        log.warn("Usuário não encontrado {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "Usuário não encontrado",
                "Não foi possivel encontrar o usuário pesquisado",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }

    // ACCOUNT

    @ExceptionHandler(AccountNameDuplicate.class)
    public ResponseEntity<ErrorResponse> handleAccountNameDuplicate(AccountNameDuplicate ex){

        log.warn("Conta com esse nome já existe {}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Conta com nome já em uso",
                "Já existe uma conta com esse nome em uso",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex){

        log.warn("Conta não encontrada {}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Conta não encontrada",
                "A conta pesquisada não foi encontrada",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    // CATEGORY
    @ExceptionHandler(CategoryNameDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNameDuplicateException(CategoryNameDuplicateException ex){

        log.warn("Categoria com nome já em uso: {}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Nome duplicado",
                "Categoria com nome já em uso",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFoundException(CategoryNotFoundException ex){

        log.warn("Categoria não encontrada: {}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Categoria não encontrada",
        "A categoria pesquisada não existe",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    // TRANSACTION

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFoundException(TransactionNotFoundException ex){

        log.warn("Transação não encontra: {}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Transação não encontrada",
                "A transação pesquisada não foi encontrada",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse>handleBusinessRuleException(BusinessRuleException ex){

        log.warn("Não é possivel apagar transações CONFIRMADAS {}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Não é possiveld etelar trasação",
                "Erro ao tentar pagar transação",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(TransactionEqualsAccountException.class)
    public ResponseEntity<ErrorResponse> handleTransactionEqualsAccountException(TransactionEqualsAccountException ex){

        log.warn("Não é possivel fazer tranferencia para a mesma conta {}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Mesma conta",
                "Não é possivel fazer transferencia para a mesma conta",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(IdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyKeyException(IdempotencyKeyException ex){

        log.warn("Transação já realizada {}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Transação já enviada",
                "A transação já foi realizada",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // GOAL

    @ExceptionHandler(GoalNameDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleGoalNameDuplicateException(GoalNameDuplicateException ex){

        log.warn("Nome desse Goal já em uso {}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Nome já em uso",
                "O nome usado já está em uso em outra META",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(GoalNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGoalNotFoundException(GoalNotFoundException ex){

        log.warn("Goal não encontrado {}", ex.getMessage());

        ErrorResponse erro = new ErrorResponse(
                "Meta não encontra",
                "A meta buscada não foi encontrada",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }
}
