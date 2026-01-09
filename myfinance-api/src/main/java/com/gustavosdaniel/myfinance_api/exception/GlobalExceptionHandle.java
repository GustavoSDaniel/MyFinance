package com.gustavosdaniel.myfinance_api.exception;

import com.gustavosdaniel.myfinance_api.accounts.AccountNameDuplicate;
import com.gustavosdaniel.myfinance_api.user.UserNotFoundException;
import jakarta.xml.bind.ValidationException;
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
}
