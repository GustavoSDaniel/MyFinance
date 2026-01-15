package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserRepository;
import com.gustavosdaniel.myfinance_api.user.UserRole;
import org.hibernate.mapping.Any;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private AccountServiceImpl accountService;

    @Nested
    class createAccount{

        @Test
        @DisplayName("Should create account with sucesso")
        void shouldCreateAccount() throws AccountNameDuplicate {

            UUID userId = UUID.randomUUID();

            User user = new User("gustavosdaniel@hmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user,"id",userId);

            AccountRequest request =
                    new AccountRequest("Conta fixa", AccountType.WALLET, "Contas do mes");

            Account newAccount = new Account(user, "Conta fixa", AccountType.WALLET, "Constas do mes");

            AccountResponse response = new AccountResponse(
                    user.getName(),
                    "Conta fixa",
                    AccountType.WALLET,
                    "Contas do mes",
                    BigDecimal.ZERO);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(accountRepository.existsByNameIgnoreCaseAndUserId(request.name(), userId)).thenReturn(false);
            when(accountMapper.toAccount(request)).thenReturn(newAccount);
            when(accountRepository.save(any(Account.class))).thenReturn(newAccount);
            when(accountMapper.toAccountResponse(newAccount)).thenReturn(response);

            AccountResponse output = accountService.createAccount(request, userId);

            assertNotNull(output);

            verify(accountMapper).toAccount(request);
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

            Account account1 = new Account(user, "Investimento", AccountType.INVESTMENT, "Tesouro direto");
            Account account2 = new Account(user, "Gasto mensal", AccountType.WALLET, "Para gastar no mes");
            Account account3 = new Account(user, "Contas dio cartao", AccountType.CREDIT_CARD, "Constas do cartão");

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
            when(accountMapper.toAccountResponseInfo(account1)).thenReturn(response1);
            when(accountMapper.toAccountResponseInfo(account2)).thenReturn(response2);
            when(accountMapper.toAccountResponseInfo(account3)).thenReturn(response3);

            List<AccountResponseInfo> output = accountService.getAllAccounts(userId);

            assertNotNull(output);
            assertEquals(3, output.size());

            verify(accountMapper, times(3)).toAccountResponseInfo(any(Account.class));
        }
    }

}