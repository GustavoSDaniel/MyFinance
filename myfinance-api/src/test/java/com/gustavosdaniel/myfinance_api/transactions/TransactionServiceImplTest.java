package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.controller.metrics.TransactionMetrics;
import com.gustavosdaniel.myfinance_api.domain.dto.request.TransactionRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.TransferRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.response.TransactionResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.TransactionSearchFilter;
import com.gustavosdaniel.myfinance_api.domain.enuns.*;
import com.gustavosdaniel.myfinance_api.domain.mapping.TransactionMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.domain.po.Transaction;
import com.gustavosdaniel.myfinance_api.exception.TransactionEqualsAccountException;
import com.gustavosdaniel.myfinance_api.repository.AccountRepository;
import com.gustavosdaniel.myfinance_api.exception.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.repository.CategoryRepository;
import com.gustavosdaniel.myfinance_api.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.exception.InsufficientBalanceException;
import com.gustavosdaniel.myfinance_api.repository.TransactionRepository;
import com.gustavosdaniel.myfinance_api.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private  CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private  AccountRepository accountRepository;

    @Mock
    private TransactionMetrics transactionMetrics;

    @InjectMocks
    private TransactionService transactionService;

    @Nested
    class createTransaction{

        @Test
        @DisplayName("Should created with sucesso transaction")
        void shouldCreateTransaction(){

            String keycloakId = "idDoKeycloak";
            UUID transactionId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryID = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            BigDecimal currentBalance = new BigDecimal("5000.00");
            LocalDate fixedDate = LocalDate.of(2025, 1, 1);
            LocalDateTime fixedDateTime = fixedDate.atStartOfDay();


            User user = new User(keycloakId,"gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            Account account = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento", null);
            ReflectionTestUtils.setField(account, "currentBalance", currentBalance);
            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryID);

            CategoryResponse categoryResponse =
                    new CategoryResponse(categoryID,"Descanso", CategoryType.DESPESA, "#008000");


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

            when(accountRepository.findByIdAndUserId(accountId, user.getId())).thenReturn(Optional.of(account));
            when(categoryRepository.findByIdAndUserId(categoryID, user.getId())).thenReturn(Optional.of(category));
            when(transactionMapper.toTransaction(
                    request, user, account, category)).thenReturn(transaction);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(transactionMapper.toTransactionResponse(transaction)).thenReturn(response);

            when(accountRepository.save(any(Account.class))).thenReturn(account);

            transactionMetrics.incrementCreated();

            TransactionResponse output = transactionService.createTransaction(user, request);

            assertNotNull(output);
            assertEquals(response, output);

            verify(transactionMapper).toTransaction(request, user, account, category);
            verify(transactionRepository).save(transaction);
            verify(transactionMapper).toTransactionResponse(transaction);
            verify(accountRepository).save(account);
        }
    }

    @Nested
    class confirmedTransaction{

        @Test
        @DisplayName("Transaction confirmed with sucesso")
        void shouldConfirmedWithSucesso(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID transactionId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            UUID categoryID = UUID.randomUUID();
            BigDecimal currentBalance = new BigDecimal("5000.00");
            BigDecimal transactionAMount = new BigDecimal("236.89");
            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 2, 10, 8, 36);

            User user = new User(keycloakId,"gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento", null);
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
        void shouldCancelWithSucesso(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID transactionId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            UUID categoryID = UUID.randomUUID();
            BigDecimal currentBalance = new BigDecimal("5000.00");
            BigDecimal transactionAMount = new BigDecimal("236.89");
            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 2, 10, 8, 36);
            TransactionStatus confirmed = TransactionStatus.CONFIRMADA;


            User user = new User(keycloakId,"gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento", null);
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
    class transfer{

        @Test
        @DisplayName("should transferaction value with sucesso")
        void shouldTransferWithSucesso(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            BigDecimal toCurrentBalance = new BigDecimal("5000.00");
            BigDecimal fromCurrentBalance = new BigDecimal("300.00");
            LocalDateTime dateTime = LocalDateTime.of(2026, 2, 10, 20, 36);

            String descriptionRequest = "Leite das crianças";
            BigDecimal amountRequest = new BigDecimal("356.89");
            LocalDate dateRequest = LocalDate.of(2026, 2, 10);

            User user = new User(keycloakId,"gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            ReflectionTestUtils.setField(user, "id", userId);

            Account fromAccount = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento", null);
            ReflectionTestUtils.setField(fromAccount, "currentBalance", toCurrentBalance);
            ReflectionTestUtils.setField(fromAccount, "id", fromAccountId);
            Account toAccount = new Account(user, "Praia", AccountType.CORRENTE, "Conta de investimento", null);
            ReflectionTestUtils.setField(toAccount, "currentBalance", fromCurrentBalance);
            ReflectionTestUtils.setField(toAccount, "id", toAccountId);

            Category category = new Category(user, "Descanso", CategoryType.TRANSFERENCIA, "#008000");
            ReflectionTestUtils.setField(category, "id", categoryId);

            Transaction from = new Transaction(
                    idempotencyKey,
                    user,
                    fromAccount,
                    category,
                    "Para curtir o feriado",
                    toCurrentBalance,
                    TransactionType.DESPESA,
                    dateTime,
                    false,
                    null);
            ReflectionTestUtils.setField(from, "id", fromAccountId);

            Transaction to = new Transaction(
                    idempotencyKey,
                    user,
                    toAccount,
                    category,
                    "Para curtir o feriado",
                    fromCurrentBalance,
                    TransactionType.RECEITA,
                    dateTime,
                    false,
                    null);
            ReflectionTestUtils.setField(to, "id", toAccountId);

            TransferRequest request = new TransferRequest(fromAccountId, toAccountId, amountRequest, categoryId, idempotencyKey, descriptionRequest, dateRequest, null, null);

            List<Account> accounts = Arrays.asList(fromAccount, toAccount);
            List<Transaction> transactions = Arrays.asList(from, to);

            when(transactionRepository.existsByIdempotencyKeyAndUserId(idempotencyKey,userId)).thenReturn(false);

            when(transactionMapper.toTransfer(
                    eq(request),
                    eq(user),
                    eq(toAccount),
                    any(Category.class),
                    eq(TransactionType.RECEITA)
            )).thenReturn(to);

            when(accountRepository.findByIdAndUserId(fromAccountId, userId)).thenReturn(Optional.of(fromAccount));
            when(accountRepository.findByIdAndUserId(toAccountId, userId)).thenReturn(Optional.of(toAccount));
            when(transactionMapper.toTransfer(eq(request), eq(user), eq(fromAccount), any(Category.class), eq(TransactionType.DESPESA)))
                    .thenReturn(from);

            when(categoryRepository.findByNameAndUserId(eq("Transferência"), eq(userId)))
                    .thenReturn(Optional.of(category));

            when(transactionRepository.saveAll(anyList())).thenReturn(transactions);
            when(accountRepository.saveAll(anyList())).thenReturn(accounts);

            transactionService.transfer(user, request);

            verify(transactionRepository).existsByIdempotencyKeyAndUserId(idempotencyKey, userId);
            verify(accountRepository).findByIdAndUserId(fromAccountId, userId);
            verify(accountRepository).findByIdAndUserId(toAccountId, userId);
            verify(categoryRepository).findByNameAndUserId("Transferência", userId);
            verify(transactionRepository).saveAll(anyList());
            verify(accountRepository).saveAll(anyList());

        }
    }

    @Nested
    class getByIdTransaction {

        @Test
        @DisplayName("Should with sucesso transaction By id")
        void shouldSucessoTransactionById() throws InvalidAmountException {

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID transactionId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryID = UUID.randomUUID();
            BigDecimal transactionAMount = new BigDecimal("236.89");
            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 2, 10, 8, 36);


            User user = new User(keycloakId,"gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento", null);
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




    @Nested
    class getAllWithFilter{

        @Test
        @DisplayName("Should get All With Filter")
        void shouldGetAllWithFilter() throws InvalidAmountException {

            Pageable pageable = PageRequest.of(0, 10);

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";

            UUID accountId = UUID.randomUUID();
            UUID accountId2 = UUID.randomUUID();
            UUID accountId3 = UUID.randomUUID();

            UUID categogryId = UUID.randomUUID();
            UUID categoryId2 = UUID.randomUUID();
            UUID categoryId3 = UUID.randomUUID();

            UUID transactionId = UUID.randomUUID();
            UUID transactionId2 = UUID.randomUUID();
            UUID transactionId3 = UUID.randomUUID();

            UUID idempotencyKey = UUID.randomUUID();
            UUID idempotencyKey2 = UUID.randomUUID();
            UUID idempotencyKey3 = UUID.randomUUID();

            BigDecimal transactionAMount = new BigDecimal("502.553");
            BigDecimal transactionAMount2 = new BigDecimal("846.79");
            BigDecimal transactionAMount3 = new BigDecimal("5024.63");

            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 8, 8, 20, 33);
            LocalDateTime fixedDateTime2 = LocalDateTime.of(2025, 6, 1, 6, 53);
            LocalDateTime fixedDateTime3 = LocalDateTime.of(2026, 2, 28, 17, 10);

            User user = new User(keycloakId,"gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            ReflectionTestUtils.setField(user, "id", userId);

            Account account = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento", null);
            Account account2 = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento", null);
            Account account3 = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento", null);

            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            Category category2 = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");
            Category category3 = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");

            CategoryResponse categoryResponse =
                    new CategoryResponse(categogryId,"Descanso", CategoryType.DESPESA, "#008000");

            CategoryResponse categoryResponse2 =
                    new CategoryResponse(categoryId2,"Descanso", CategoryType.DESPESA, "#008000");

            CategoryResponse categoryResponse3 =
                    new CategoryResponse(categoryId3,"Descanso", CategoryType.DESPESA, "#008000");

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

            Transaction transaction2 = new Transaction(
                    idempotencyKey2,
                    user,
                    account2,
                    category2,
                    "Para curtir o feriado",
                    transactionAMount2,
                    TransactionType.DESPESA,
                    fixedDateTime2,
                    false,
                    null);
            ReflectionTestUtils.setField(transaction2, "id", transactionId2);

            Transaction transaction3 = new Transaction(
                    idempotencyKey3,
                    user,
                    account3,
                    category3,
                    "Para curtir o feriado",
                    transactionAMount3,
                    TransactionType.DESPESA,
                    fixedDateTime3,
                    false,
                    null);
            ReflectionTestUtils.setField(transaction3, "id", transactionId3);

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

            TransactionResponse response2 = new TransactionResponse(
                    transactionId2,
                    "Role do fina de semana",
                    new BigDecimal("1352.69"),
                    TransactionType.DESPESA,
                    LocalDateTime.now(),
                    TransactionStatus.PENDENTE,
                    accountId2,
                    account.getName(),
                    categoryResponse2,
                    false,
                    null);

            TransactionResponse response3 = new TransactionResponse(
                    transactionId3,
                    "Role do fina de semana",
                    new BigDecimal("1352.69"),
                    TransactionType.DESPESA,
                    LocalDateTime.now(),
                    TransactionStatus.PENDENTE,
                    accountId3,
                    account.getName(),
                    categoryResponse3,
                    false,
                    null);

            TransactionSearchFilter filter = new TransactionSearchFilter(null, null, null, null, null, null, null);

            List<Transaction> transactions = Arrays.asList(transaction, transaction2, transaction3);

            Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, transactions.size());

            when(transactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(transactionPage);
            when(transactionMapper.toTransactionResponse(transaction)).thenReturn(response);
            when(transactionMapper.toTransactionResponse(transaction2)).thenReturn(response2);
            when(transactionMapper.toTransactionResponse(transaction3)).thenReturn(response3);

            Page<TransactionResponse> output = transactionService.getAllWithFilter(user, filter, pageable);

            assertNotNull(output);
            assertEquals(3, output.getTotalElements());


            verify(transactionRepository, times(1)).findAll(any(Specification.class), eq(pageable));
            verify(transactionMapper).toTransactionResponse(transaction);
            verify(transactionMapper).toTransactionResponse(transaction2);
            verify(transactionMapper).toTransactionResponse(transaction3);

        }
    }

    @Nested
    class deleteTransaction{

        @Test
        @DisplayName("Should delete transaction with sucesso")
        void shouldDeleteTransaction() throws InvalidAmountException {

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID idempotencyKey = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            BigDecimal transactionAMount = new BigDecimal("236.89");
            LocalDateTime fixedDateTime = LocalDateTime.of(2026, 2, 10, 8, 36);

            User user = new User(keycloakId,"gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Viajem", AccountType.CORRENTE, "Conta de investimento", null);
            Category category = new Category(user, "Descanso", CategoryType.DESPESA, "#008000");

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

            when(transactionRepository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.of(transaction));

            transactionService.deleteTransaction(transactionId, userId);

            verify(transactionRepository).findByIdAndUserId(transactionId, userId);

        }
    }
}