package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.accounts.Account;
import com.gustavosdaniel.myfinance_api.accounts.AccountRepository;
import com.gustavosdaniel.myfinance_api.accounts.AccountType;
import com.gustavosdaniel.myfinance_api.accounts.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.categories.CategoryRepository;
import com.gustavosdaniel.myfinance_api.categories.CategoryResponse;
import com.gustavosdaniel.myfinance_api.categories.CategoryType;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserRole;
import com.gustavosdaniel.myfinance_api.util.InsufficientBalanceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private  CategoryRepository categoryRepository;

    @Mock
    private  TransactionRepository transactionRepository;

    @Mock
    private  TransactionMapper transactionMapper;

    @Mock
    private  AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Nested
    class createTransaction{

        @Test
        @DisplayName("Should created with sucesso transaction")
        void shouldCreateTransaction() throws InvalidAmountException, InsufficientBalanceException {

            UUID transactionId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryID = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            BigDecimal currentBalance = new BigDecimal("5000.00");
            LocalDate fixedDate = LocalDate.of(2025, 1, 1);
            LocalDateTime fixedDateTime = fixedDate.atStartOfDay();


            User user = new User("gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            Account account = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento");
            ReflectionTestUtils.setField(account, "currentBalance", currentBalance);
            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryID);

            CategoryResponse categoryResponse =
                    new CategoryResponse(categoryID,"Descanso", CategoryType.DESPESA, "#008000");

            TransactionProfile profile = new TransactionProfile(user, account, category);

            TransactionRequest request = new TransactionRequest(
                    "Para meu amigo",
                    new BigDecimal("1352.69"),
                    TransactionType.DESPESA,
                    accountId,
                    categoryID,
                    fixedDate,
                    false,
                    null,
                    idempotencyKey);

            Transaction transaction = new Transaction(
                    idempotencyKey,
                    user,
                    account,
                    category,
                    "Para curtir o feriado",
                    request.amount(),
                    request.type(),
                    fixedDateTime,
                    false,
                    null);
            ReflectionTestUtils.setField(transaction, "id", transactionId);

            TransactionResponse response = new TransactionResponse(
                    transactionId,
                    "Role do fina de semana",
                    new BigDecimal("1352.69"),
                    TransactionType.DESPESA,
                    LocalDateTime.now(),
                    TransactionStatus.PENDENTE,
                    accountId,
                    account.getName(),
                    categoryResponse,
                    false,
                    null);

            when(transactionMapper.toTransaction(
                    request, profile.user(), profile.account(), profile.category())).thenReturn(transaction);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(transactionMapper.toTransactionResponse(transaction)).thenReturn(response);

            when(accountRepository.save(any(Account.class))).thenReturn(account);

            TransactionResponse output = transactionService.createTransaction(profile, request);

            assertNotNull(output);
            assertEquals(response, output);

            verify(transactionMapper).toTransaction(request, profile.user(), profile.account(), profile.category());
            verify(transactionRepository).save(transaction);
            verify(transactionMapper).toTransactionResponse(transaction);
            verify(accountRepository).save(account);
        }
    }

    @Nested
    class confirmedTransaction{

        @Test
        @DisplayName("Transaction confirmed with sucesso")
        void shouldConfirmedWithSucesso() throws InvalidAmountException, InsufficientBalanceException {

            UUID userId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            UUID categoryID = UUID.randomUUID();
            BigDecimal currentBalance = new BigDecimal("5000.00");
            BigDecimal transactionAMount = new BigDecimal("236.89");
            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 2, 10, 8, 36);

            User user = new User("gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento");
            ReflectionTestUtils.setField(account, "currentBalance", currentBalance);
            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryID);

            Transaction transaction = new Transaction(
                    idempotencyKey,
                    user,
                    account,
                    category,
                    "Para curtir o feriado",
                    transactionAMount,
                    TransactionType.DESPESA,
                    fixedDateTime,
                    false,
                    null);
            ReflectionTestUtils.setField(transaction, "id", transactionId);

            when(transactionRepository
                    .findByIdAndUserId(transactionId, userId)).thenReturn(Optional.of(transaction));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(accountRepository.save(any(Account.class))).thenReturn(account);

            transactionService.transactionConfirmed(transactionId, userId);

            verify(transactionRepository).findByIdAndUserId(transactionId, userId);
            verify(transactionRepository).save(transaction);
            verify(accountRepository).save(account);
        }
    }

    @Nested
    class cancelTransaction{

        @Test
        @DisplayName("Should cancel transaction with sucesso")
        void shouldCancelWithSucesso() throws InvalidAmountException, InsufficientBalanceException {

            UUID userId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            UUID categoryID = UUID.randomUUID();
            BigDecimal currentBalance = new BigDecimal("5000.00");
            BigDecimal transactionAMount = new BigDecimal("236.89");
            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 2, 10, 8, 36);
            TransactionStatus confirmed = TransactionStatus.CONFIRMADA;


            User user = new User("gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento");
            ReflectionTestUtils.setField(account, "currentBalance", currentBalance);
            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryID);;

            Transaction transaction = new Transaction(
                    idempotencyKey,
                    user,
                    account,
                    category,
                    "Para curtir o feriado",
                    transactionAMount,
                    TransactionType.DESPESA,
                    fixedDateTime,
                    false,
                    null);
            ReflectionTestUtils.setField(transaction, "id", transactionId);
            ReflectionTestUtils.setField(transaction, "status", confirmed );


            when(transactionRepository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.of(transaction));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(accountRepository.save(any(Account.class))).thenReturn(account);


            transactionService.transactionCancel(transactionId, userId);

            verify(transactionRepository).findByIdAndUserId(transactionId, userId);
            verify(transactionRepository).save(transaction);
            verify(accountRepository).save(account);

        }
    }

    @Nested
    class getByIdTransaction {

        @Test
        @DisplayName("Should with sucesso transaction By id")
        void shouldSucessoTransactionById() throws InvalidAmountException {

            UUID userId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryID = UUID.randomUUID();
            BigDecimal transactionAMount = new BigDecimal("236.89");
            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 2, 10, 8, 36);


            User user = new User("gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento");
            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");

            CategoryResponse categoryResponse =
                    new CategoryResponse(categoryID,"Descanso", CategoryType.DESPESA, "#008000");

            Transaction transaction = new Transaction(
                    idempotencyKey,
                    user,
                    account,
                    category,
                    "Para curtir o feriado",
                    transactionAMount,
                    TransactionType.DESPESA,
                    fixedDateTime,
                    false,
                    null);
            ReflectionTestUtils.setField(transaction, "id", transactionId);

            TransactionResponse response = new TransactionResponse(
                    transactionId,
                    "Role do fina de semana",
                    new BigDecimal("1352.69"),
                    TransactionType.DESPESA,
                    LocalDateTime.now(),
                    TransactionStatus.PENDENTE,
                    accountId,
                    account.getName(),
                    categoryResponse,
                    false,
                    null);

            when(transactionRepository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.of(transaction));
            when(transactionMapper.toTransactionResponse(transaction)).thenReturn(response);

            TransactionResponse output = transactionService.getTransactionById(transactionId, userId);

            assertNotNull(output);
            assertEquals(response, output);

            verify(transactionRepository).findByIdAndUserId(transactionId, userId);
            verify(transactionMapper).toTransactionResponse(transaction);
        }
    }
}