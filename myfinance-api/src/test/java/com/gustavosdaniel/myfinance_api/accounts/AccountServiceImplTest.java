package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.domain.dto.request.AccountRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.response.AccountResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.dto.request.AccountUpdateRequest;
import com.gustavosdaniel.myfinance_api.domain.enuns.AccountType;
import com.gustavosdaniel.myfinance_api.domain.mapping.AccountMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.exception.AccountNameDuplicateException;
import com.gustavosdaniel.myfinance_api.repository.AccountRepository;
import com.gustavosdaniel.myfinance_api.service.AccountService;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.repository.UserRepository;
import com.gustavosdaniel.myfinance_api.domain.enuns.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    @Nested
    class createAccount{

        @Test
        @DisplayName("Should create account with sucesso")
        void shouldCreateAccount() throws AccountNameDuplicateException {

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";

            User user = new User(keycloakId,"gustavosdaniel@hmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user,"id",userId);

            AccountRequest request =
                    new AccountRequest("Conta fixa", AccountType.WALLET, null ,"Contas do mes");

            Account newAccount = new Account(user, "Conta fixa", AccountType.WALLET, "Constas do mes", null);

            AccountResponse response = new AccountResponse(
                    UUID.randomUUID(),
                    user.getName(),
                    "Conta fixa",
                    AccountType.WALLET,
                    "Contas do mes",
                    BigDecimal.ZERO);

            when(accountRepository.existsByNameIgnoreCaseAndUserId(request.name(), userId)).thenReturn(false);
            when(accountMapper.toAccount(user, request)).thenReturn(newAccount);
            when(accountRepository.save(any(Account.class))).thenReturn(newAccount);
            when(accountMapper.toAccountResponse(newAccount)).thenReturn(response);

            AccountResponse output = accountService.createAccount(request, user);

            assertNotNull(output);

            verify(accountMapper).toAccount(user, request);
            verify(accountRepository).save(newAccount);
            verify(accountMapper).toAccountResponse(newAccount);

        }
    }

    @Nested
    class getAllAccounts{

        @Test
        @DisplayName("Should all accounts with sucesso")
        void shouldAllAccounts(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";

            User user = new User(keycloakId, "gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Account account1 = new Account(user, "Investimento", AccountType.INVESTMENT, "Tesouro direto", null);
            Account account2 = new Account(user, "Gasto mensal", AccountType.WALLET, "Para gastar no mes", null);
            Account account3 = new Account(user, "Contas dio cartao", AccountType.CREDIT_CARD, "Constas do cartão", null);

            List<Account> accounts = Arrays.asList(account1, account2, account3);

            AccountResponseInfo response1 = new AccountResponseInfo(
                    userId,
                    user.getName(), "Investimento",
                    AccountType.INVESTMENT,
                    "Tesouro direto",
                    BigDecimal.valueOf(10000));

            AccountResponseInfo response2 = new AccountResponseInfo(
                    userId,
                    user.getName(),
                    "Gasto mensal",
                    AccountType.WALLET,
                    "Para gastar no mes",
                    new BigDecimal("1500.47"));

            AccountResponseInfo response3 = new AccountResponseInfo(
                    userId,
                    user.getName(),
                    "Contas dio cartao",
                    AccountType.CREDIT_CARD,
                    "Constas do cartão",
                    new BigDecimal("869.69"));

            when(accountRepository.findByUserId(userId)).thenReturn(accounts);
            when(accountMapper.toAccountResponseInfo(account1)).thenReturn(response1);
            when(accountMapper.toAccountResponseInfo(account2)).thenReturn(response2);
            when(accountMapper.toAccountResponseInfo(account3)).thenReturn(response3);

            List<AccountResponseInfo> output = accountService.getAllAccounts(userId, "status");

            assertNotNull(output);
            assertEquals(3, output.size());

            verify(accountMapper, times(3)).toAccountResponseInfo(any(Account.class));
        }
    }


    @Nested
    class getByIdAccount{

        @Test
        @DisplayName("Should accounts by id with sucesso")
        void shouldAccountBtId(){

            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";

            User user = new User( keycloakId,"gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Account account = new Account(user, "Poupança",AccountType.POUPANCA, "Fundo de emergencia", null);
            ReflectionTestUtils.setField(account, "id", accountId);

            AccountResponseInfo response = new AccountResponseInfo(
                    userId,
                    user.getName(),
                    "Poupança",
                    AccountType.POUPANCA,
                    "Fundo de emergencia",
                    new BigDecimal("10800.63"));

            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            when(accountMapper.toAccountResponseInfo(account)).thenReturn(response);

            AccountResponseInfo output = accountService.getById(accountId, userId);

            assertNotNull(output);
            assertEquals(output, response);

            verify(accountRepository).findByIdAndUserId(accountId, userId);
            verify(accountMapper).toAccountResponseInfo(account);
        }
    }

    @Nested
    class searchAccount{

        @Test
        @DisplayName("Should search account with sucesso")
        void searchAccount(){

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";

            User user = new User( keycloakId,"gustavosdaniel@hotmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Account account = new Account(user, "Constas fixa", AccountType.WALLET, "Constas do mes", null);
            Account account2 = new Account(user, "Poupança", AccountType.POUPANCA, "Fundo de emergencia", null);
            Account account3= new Account(user, "Cartao", AccountType.CREDIT_CARD, "Fatura do cartão", null);

            List<Account> accounts = Arrays.asList(account, account2, account3);

            AccountResponseInfo response = new AccountResponseInfo(
                    userId,
                    user.getName(),
                    "Constas fixa",
                    AccountType.WALLET,
                    "Constas do mes",
                    BigDecimal.valueOf(5000));

            AccountResponseInfo response2 = new AccountResponseInfo(
                    userId,
                    user.getName(),
                    "Poupança",
                    AccountType.POUPANCA,
                    "Fundo de emergencia",
                    new BigDecimal("10300.58"));

            AccountResponseInfo response3 = new AccountResponseInfo(
                    userId,
                    user.getName(),
                    "Cartao",
                    AccountType.CREDIT_CARD,
                    "Fatura do cartão",
                    new BigDecimal("7834.57"));

            when(accountRepository.searchByName(anyString(), eq(userId))).thenReturn(accounts);
            when(accountMapper.toAccountResponseInfo(account)).thenReturn(response);
            when(accountMapper.toAccountResponseInfo(account2)).thenReturn(response2);
            when(accountMapper.toAccountResponseInfo(account3)).thenReturn(response3);

            List<AccountResponseInfo> output = accountService.searchAccount("car", userId);

            assertNotNull(output);
            assertEquals(3, output.size());

            verify(accountRepository).searchByName("car", userId);
            verify(accountMapper).toAccountResponseInfo(account3);

        }
    }

    @Nested
    class updatedAccount{

        @Test
        @DisplayName("Should updated a information account with sucesso")
        void updateAccount() throws AccountNameDuplicateException {

            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";
            UUID accountId = UUID.randomUUID();

            User user = new User( keycloakId,"gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Conta de test", AccountType.POUPANCA, "Conta para testar", null);
            ReflectionTestUtils.setField(account, "id", accountId);

            AccountUpdateRequest request = new AccountUpdateRequest("Conta de test update","Conta de teste para atualizar", AccountType.WALLET);
            AccountResponseInfo response = new AccountResponseInfo(userId,user.getName(), "Conta de test update",AccountType.WALLET, "Conta de teste para atualizar", new BigDecimal("3325.69"));

            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            accountMapper.updateAccountFromRequest(request, account);
            when(accountRepository.save(any(Account.class))).thenReturn(account);
            when(accountMapper.toAccountResponseInfo(account)).thenReturn(response);

            AccountResponseInfo output = accountService.updateAccount(accountId, userId, request);

            assertNotNull(output);
            assertEquals(output, response);

            verify(accountRepository).findByIdAndUserId(accountId, userId);
            verify(accountRepository).save(any(Account.class));
            verify(accountMapper).toAccountResponseInfo(account);

        }
    }

    @Nested
    class activateAccount{

        @Test
        @DisplayName("Should activate account with sucesso")
        void activateAccount(){

            UUID accountId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";

            User user = new User(keycloakId,"gustavosdaniel@gmail.com", "gustavo", UserRole.ADMIN);
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Conta par ativar", AccountType.POUPANCA, "Ativando conta poupança", null);
            ReflectionTestUtils.setField(account, "id", accountId);

            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            account.setActive(false);

            accountService.activateAccount(accountId, userId);

            verify(accountRepository).findByIdAndUserId(accountId, userId);

        }
    }

    @Nested
    class deactivateAccount{

        @Test
        @DisplayName("Should deactivate account with sucesso")
        void deactivateAccount(){

            UUID accountId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";

            User user = new User(keycloakId,"gustavosdaniel@gmail.com", "gustavo", UserRole.ADMIN);
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Conta para desativar", AccountType.POUPANCA, "Desativando conta poupança", null);
            ReflectionTestUtils.setField(account, "id", accountId);

            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            account.setActive(true);

            accountService.deactivateAccount(accountId, userId);

            verify(accountRepository).findByIdAndUserId(accountId, userId);

        }
    }

    @Nested
    class deletedAccount{

        @Test
        @DisplayName("Should deleted account with sucesso")
        void deletedAccount(){

            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            String keycloakId = "idDoKeycloak";

            User user = new User(keycloakId,"email@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Cartáo de credito", AccountType.CREDIT_CARD,"Apagando conta", null );
            ReflectionTestUtils.setField(account, "id", accountId);

            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));

            accountService.deleteAccount(accountId, user);

            verify(accountRepository).findByIdAndUserId(accountId, userId);
        }
    }
}