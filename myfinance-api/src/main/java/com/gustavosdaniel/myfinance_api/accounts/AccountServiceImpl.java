package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserMapper;
import com.gustavosdaniel.myfinance_api.user.UserNotFoundException;
import com.gustavosdaniel.myfinance_api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService{

    private final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserRepository userRepository;

    public AccountServiceImpl(AccountRepository accountRepository, AccountMapper accountMapper, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.userRepository = userRepository;
    }


    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest accountRequest, UUID userId) throws AccountNameDuplicate {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        log.info("Criando uma nova conta para o usuário {}", user.getName());

        Account newAccount = accountMapper.toAccount(accountRequest);

        if (accountRepository.existsByNameIgnoreCaseAndUserId(accountRequest.name(), user.getId())){

            throw new AccountNameDuplicate();
        }

        newAccount.setUser(user);

        Account savedAccount = accountRepository.save(newAccount);

        user.addAccount(newAccount);

        log.info("Nova conta {} adicionada para o usuário: {}", newAccount.getName(), user.getName());

        return accountMapper.toAccountResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getById(UUID id, UUID userId){

        log.info("Buscando conta {} para o usuário {}", id, userId);

        Account account = accountRepository.findByIdAndUserId(id, userId).orElseThrow(AccountNotFoundException::new);

        log.info("Conta encontrada com sucesso");

        return accountMapper.toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> searchAccount(String name, UUID userId) {

        log.info("Buscando contas pelo nome");

        List<Account> accounts = accountRepository.searchByName(name, userId);

        log.info("Contas encontradas com sucesso {}", accounts);

        return accounts.stream().map(accountMapper::toAccountResponse).toList();
    }


}
