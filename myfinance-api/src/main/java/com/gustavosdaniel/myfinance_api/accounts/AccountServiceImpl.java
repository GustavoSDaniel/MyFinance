package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.transactions.TransactionType;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.user.UserNotFoundException;
import com.gustavosdaniel.myfinance_api.user.UserRepository;
import com.gustavosdaniel.myfinance_api.util.InsufficientBalanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

        if (accountRepository.existsByNameIgnoreCaseAndUserId(accountRequest.name(), user.getId())){

            throw new AccountNameDuplicate();
        }

        log.info("Criando uma nova conta para o usuário: {}", user.getName());

        Account newAccount = accountMapper.toAccount(accountRequest);

        newAccount.setUser(user);

        Account savedAccount = accountRepository.save(newAccount);

        user.addAccount(newAccount);

        log.info("Nova conta: {} adicionada para o usuário: {}", newAccount.getName(), user.getName());

        return accountMapper.toAccountResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseInfo> getAllAccounts(UUID userId) {

        log.info("Buscando todas as contas do usuário: {}", userId);

        List<Account> accounts = accountRepository.findByUserId(userId);

        log.info("Total de contas encontradas: {}", accounts.size());

        return accounts.stream().map(accountMapper::toAccountResponseInfo).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseInfo> getAllAccountsActive(UUID userId) {

        log.info("Buscando todas as contas do usuário: {} ativas", userId);

        List<Account> accountsActive =accountRepository.findByUserIdAndIsActiveTrue(userId);

        log.info("Todas as contas ativas encontradas {}", accountsActive.size());

        return accountsActive.stream().map(accountMapper::toAccountResponseInfo).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseInfo> getAllAccountsDisabled(UUID userId) {

        log.info("Buscando todas as contas do usuário: {} desativadas", userId);

        List<Account> accountsDisabled = accountRepository.findByUserIdAndIsActiveFalse(userId);

        log.info("Todas as contas desativadas encontradas {}", accountsDisabled.size());

        return accountsDisabled.stream().map(accountMapper::toAccountResponseInfo).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseInfo getById(UUID id, UUID userId){

        log.info("Buscando conta {} para o usuário {}", id, userId);

        Account account = accountRepository.findByIdAndUserId(id, userId).orElseThrow(AccountNotFoundException::new);

        log.info("Conta encontrada com sucesso");

        return accountMapper.toAccountResponseInfo(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseInfo> searchAccount(String name, UUID userId) {

        log.info("Buscando contas pelo nome");

        List<Account> accounts = accountRepository.searchByName(name, userId);

        log.info("Contas encontradas com sucesso {}", accounts.size());

        return accounts.stream().map(accountMapper::toAccountResponseInfo).toList();
    }

    @Override
    @Transactional
    public void updateBalance(UUID id, UUID userId, BigDecimal value, TransactionType type) throws InvalidAmountException, InsufficientBalanceException {

        Account account = accountRepository.findByIdAndUserId(id, userId).orElseThrow(AccountNotFoundException::new);

        if (type == TransactionType.RECEITA){

            account.addBalance(value);

        }else if (type == TransactionType.DESPESA){
            account.removeBalance(value);
        }

    }

    @Override
    @Transactional
    public AccountResponseInfo updateAccount(UUID id, UUID userId, AccountUpdateRequest request) throws AccountNameDuplicate {

        Account account = accountRepository.findByIdAndUserId(id, userId).orElseThrow(AccountNotFoundException::new);

        log.info("Atualizando informações da conta: {}", account.getName());

        if (request.name() != null && !account.getName().equalsIgnoreCase(request.name())){

            if (accountRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(request.name(), userId, id)){

                    throw new AccountNameDuplicate();
            }
        }

        accountMapper.updateAccountFromRequest(request, account);

        Account accountUpdated = accountRepository.save(account);

        log.info("Conta: {} atualizada com sucesso", accountUpdated.getName());

        return accountMapper.toAccountResponseInfo(accountUpdated);
    }

    @Override
    @Transactional
    public void activateAccount(UUID id, UUID userId) {

        log.info("Ativando conta com id: {}", id);

        Account account = accountRepository.findByIdAndUserId(id, userId).orElseThrow(AccountNotFoundException::new);

        if (Boolean.TRUE.equals(account.getActive())){
            log.warn("Tentativa de ativar conta que já está ativa: {}", id);
            return;
        }

        account.setActive(true);

        log.info("Conta: {} ativada com sucesso pelo usuário {}", account.getName(), userId);
    }

    @Override
    @Transactional
    public void deactivateAccount(UUID id, UUID userId) {

        log.info("Desativando conta com id: {}", id);

        Account account = accountRepository.findByIdAndUserId(id, userId).orElseThrow(AccountNotFoundException::new);

        if (Boolean.FALSE.equals(account.getActive())){
            log.warn("Tentativa de desativar conta que já está inativa: {}", id);
            return;
        }

        account.setActive(false);

        log.info("Conta: {} desativada com sucesso pelo usuário {}", account.getName(), userId);
    }

    @Override
    @Transactional
    public void deleteAccount(UUID id, UUID userId) {

        Account account = accountRepository.findByIdAndUserId(id, userId).orElseThrow(AccountNotFoundException::new);

        log.warn("Deletando conta permanentemente: {} do usuário {}", account.getName(), account.getUser().getName());

        accountRepository.delete(account);

    }


}
