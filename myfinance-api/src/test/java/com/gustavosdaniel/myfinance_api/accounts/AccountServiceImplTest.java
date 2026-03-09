package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.domain.dto.AccountRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountUpdateRequest;
import com.gustavosdaniel.myfinance_api.domain.enuns.AccountType;
import com.gustavosdaniel.myfinance_api.domain.mapping.AccountMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.exception.AccountNameDuplicateException;
import com.gustavosdaniel.myfinance_api.repository.AccountRepository;
import com.gustavosdaniel.myfinance_api.service.AccountService;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserRole;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private AccountMapper accountMapper;

    @Mock
    private AuthHelper authHelper;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private AccountService accountService;

    @Nested
    class createAccount{

        @Test
        @DisplayName("Should create account with sucesso")
        void shouldCreateAccount(){

            MockHttpServletRequest httpRequest = new MockHttpServletRequest();
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

            UUID userId = UUID.randomUUID();

            User user = new User("gustavosdaniel@hmail.com", "Gustavo", UserRole.USER);
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

            when(accountRepository.existsByNameIgnoreCaseAndUserId(request.name().trim(), userId)).thenReturn(false);
            when(accountMapper.toAccount(user, request)).thenReturn(newAccount);
            when(accountRepository.save(any(Account.class))).thenReturn(newAccount);
            when(accountMapper.toAccountResponse(newAccount)).thenReturn(response);
            when(authHelper.getCurrentUser(principal)).thenReturn(user);

            ResponseEntity<AccountResponse> output = accountService.createAccount(request, principal);

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

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Account account1 = new Account(user, "Investimento", AccountType.INVESTMENT, "Tesouro direto", null);
            Account account2 = new Account(user, "Gasto mensal", AccountType.WALLET, "Para gastar no mes", null);
            Account account3 = new Account(user, "Contas dio cartao", AccountType.CREDIT_CARD, "Constas do cartão", null);

            List<Account> accounts = Arrays.asList(account1, account2, account3);

            AccountResponseInfo response1 = new AccountResponseInfo(
                    user.getName(), "Investimento",
                    AccountType.INVESTMENT,
                    "Tesouro direto",
                    BigDecimal.valueOf(10000));

            AccountResponseInfo response2 = new AccountResponseInfo(
                    user.getName(),
                    "Gasto mensal",
                    AccountType.WALLET,
                    "Para gastar no mes",
                    new BigDecimal("1500.47"));

            AccountResponseInfo response3 = new AccountResponseInfo(
                    user.getName(),
                    "Contas dio cartao",
                    AccountType.CREDIT_CARD,
                    "Constas do cartão",
                    new BigDecimal("869.69"));

            when(accountRepository.findByUserId(userId)).thenReturn(accounts);
            when(authHelper.getCurrentUser(principal)).thenReturn(user);
            when(accountMapper.toAccountResponseInfo(account1)).thenReturn(response1);
            when(accountMapper.toAccountResponseInfo(account2)).thenReturn(response2);
            when(accountMapper.toAccountResponseInfo(account3)).thenReturn(response3);

            ResponseEntity<List<AccountResponseInfo>> output = accountService.getAllAccounts(principal, "status");

            assertNotNull(output);
            assertEquals(3, output.getBody().size());

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

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Account account = new Account(user, "Poupança",AccountType.POUPANCA, "Fundo de emergencia", null);
            ReflectionTestUtils.setField(account, "id", accountId);

            AccountResponseInfo response = new AccountResponseInfo(
                    user.getName(),
                    "Poupança",
                    AccountType.POUPANCA,
                    "Fundo de emergencia",
                    new BigDecimal("10800.63"));

            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            when(accountMapper.toAccountResponseInfo(account)).thenReturn(response);
            when(authHelper.getCurrentUser(principal)).thenReturn(user);

            ResponseEntity<AccountResponseInfo> output = accountService.getById(accountId, principal);

            assertNotNull(output);
            assertEquals(HttpStatus.OK, output.getStatusCode());
            assertEquals(response, output.getBody());

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

            User user = new User("gustavosdaniel@hotmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Account account = new Account(user, "Constas fixa", AccountType.WALLET, "Constas do mes", null);
            Account account2 = new Account(user, "Poupança", AccountType.POUPANCA, "Fundo de emergencia", null);
            Account account3= new Account(user, "Cartao", AccountType.CREDIT_CARD, "Fatura do cartão", null);

            List<Account> accounts = Arrays.asList(account, account2, account3);

            AccountResponseInfo response = new AccountResponseInfo(
                    user.getName(),
                    "Constas fixa",
                    AccountType.WALLET,
                    "Constas do mes",
                    BigDecimal.valueOf(5000));

            AccountResponseInfo response2 = new AccountResponseInfo(
                    user.getName(),
                    "Poupança",
                    AccountType.POUPANCA,
                    "Fundo de emergencia",
                    new BigDecimal("10300.58"));

            AccountResponseInfo response3 = new AccountResponseInfo(
                    user.getName(),
                    "Cartao",
                    AccountType.CREDIT_CARD,
                    "Fatura do cartão",
                    new BigDecimal("7834.57"));

            when(accountRepository.searchByName(anyString(), eq(userId))).thenReturn(accounts);
            when(accountMapper.toAccountResponseInfo(account)).thenReturn(response);
            when(accountMapper.toAccountResponseInfo(account2)).thenReturn(response2);
            when(accountMapper.toAccountResponseInfo(account3)).thenReturn(response3);
            when(authHelper.getCurrentUser(principal)).thenReturn(user);

            ResponseEntity<List<AccountResponseInfo>> output = accountService.searchAccount("car", principal);

            assertNotNull(output);
            assertEquals(3, output.getBody().size());

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
            UUID accountId = UUID.randomUUID();

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Conta de test", AccountType.POUPANCA, "Conta para testar", null);
            ReflectionTestUtils.setField(account, "id", accountId);

            AccountUpdateRequest request = new AccountUpdateRequest("Conta de test update","Conta de teste para atualizar", AccountType.WALLET);
            AccountResponseInfo response = new AccountResponseInfo(user.getName(), "Conta de test update",AccountType.WALLET, "Conta de teste para atualizar", new BigDecimal("3325.69"));

            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            accountMapper.updateAccountFromRequest(request, account);
            when(accountRepository.save(any(Account.class))).thenReturn(account);
            when(accountMapper.toAccountResponseInfo(account)).thenReturn(response);
            when(authHelper.getCurrentUser(principal)).thenReturn(user);


            ResponseEntity<AccountResponseInfo> output = accountService.updateAccount(accountId, principal, request);

            assertNotNull(output);
            assertEquals(HttpStatus.OK, output.getStatusCode());
            assertEquals(output.getBody(), response);

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

            User user = new User("gustavosdaniel@gmail.com", "gustavo", UserRole.ADMIN);
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Conta par ativar", AccountType.POUPANCA, "Ativando conta poupança", null);
            ReflectionTestUtils.setField(account, "id", accountId);

            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            when(authHelper.getCurrentUser(principal)).thenReturn(user);

            account.setActive(false);

            accountService.activateAccount(accountId, principal);

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

            User user = new User("gustavosdaniel@gmail.com", "gustavo", UserRole.ADMIN);
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Conta para desativar", AccountType.POUPANCA, "Desativando conta poupança", null);
            ReflectionTestUtils.setField(account, "id", accountId);

            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            when(authHelper.getCurrentUser(principal)).thenReturn(user);

            account.setActive(true);

            accountService.deactivateAccount(accountId, principal);

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

            User user = new User("email@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);
            Account account = new Account(user, "Cartáo de credito", AccountType.CREDIT_CARD,"Apagando conta", null );
            ReflectionTestUtils.setField(account, "id", accountId);

            when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
            when(authHelper.getCurrentUser(principal)).thenReturn(user);

            accountService.deleteAccount(accountId, principal);

            verify(accountRepository).findByIdAndUserId(accountId, userId);
        }
    }
}