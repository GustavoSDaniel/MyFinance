package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.accounts.AccountRepository;
import com.gustavosdaniel.myfinance_api.categories.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

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
        void shouldCreateTransaction(){


        }
    }



}