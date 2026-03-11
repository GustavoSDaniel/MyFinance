package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.exception.AccountNameDuplicate;
import com.gustavosdaniel.myfinance_api.exception.AccountNotFoundException;
import com.gustavosdaniel.myfinance_api.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@CacheConfig(cacheNames = "accounts")
public class AccountService {

    private final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper ) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }


    @Transactional
    @CacheEvict(allEntries = true)
    public AccountResponse createAccount(AccountRequest accountRequest, User user) throws AccountNameDuplicate {

        if (accountRepository.existsByNameIgnoreCaseAndUserId(accountRequest.name().trim(), user.getId())){

            throw new AccountNameDuplicate();
        }

        log.info("Criando uma nova conta para o usuário: {}", user.getName());

        Account newAccount = accountMapper.toAccount(user, accountRequest);

        Account savedAccount = accountRepository.save(newAccount);

        user.addAccount(newAccount);

        log.info("Nova conta: {} adicionada para o usuário: {}", newAccount.getName(), user.getName());

        return accountMapper.toAccountResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#userId + '_' + #status")
    public List<AccountResponseInfo> getAllAccounts(UUID userId, String status) {

        log.info("Buscando todas as contas do usuário: {} com status: {}", userId, status);

        List<Account> accounts;

        if ("active".equalsIgnoreCase(status)){

            accounts = accountRepository.findByUserIdAndIsActiveTrue(userId);

        }  else if ("disabled".equalsIgnoreCase(status)) {

            accounts = accountRepository.findByUserIdAndIsActiveFalse(userId);
        } else {

            accounts = accountRepository.findByUserId(userId);
        }

        log.info("Total de contas encontradas: {}", accounts.size());

        return accounts.stream().map(accountMapper::toAccountResponseInfo).toList();
    }


    @Transactional(readOnly = true)
    @Cacheable(key = "#id + '_' + #userId")
    public AccountResponseInfo getById(UUID id, UUID userId){

        log.info("Buscando conta {} para o usuário {}", id, userId);

        Account account = accountRepository.findByIdAndUserId(id, userId).orElseThrow(AccountNotFoundException::new);

        log.info("Conta encontrada com sucesso");

        return accountMapper.toAccountResponseInfo(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponseInfo> searchAccount(String name, UUID userId) {

        log.info("Buscando contas pelo nome");

        List<Account> accounts = accountRepository.searchByName(name, userId);

        log.info("Contas encontradas com sucesso {}", accounts.size());

        return accounts.stream().map(accountMapper::toAccountResponseInfo).toList();
    }

    @Transactional
    @CacheEvict(allEntries = true)
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

    @Transactional
    @CacheEvict(allEntries = true)
    public void activateAccount(UUID id, UUID userId) {

        log.info("Ativando conta com id: {}", id);

        Account account = accountRepository.findByIdAndUserId(id, userId).orElseThrow(AccountNotFoundException::new);

        if (Boolean.TRUE.equals(account.getActive())){
            log.warn("Tentativa de ativar conta que já está ativa: {}", id);
            return;
        }

        account.setActive(true);

        accountRepository.save(account);

        log.info("Conta: {} ativada com sucesso pelo usuário {}", account.getName(), userId);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void deactivateAccount(UUID id, UUID userId) {

        log.info("Desativando conta com id: {}", id);

        Account account = accountRepository.findByIdAndUserId(id, userId).orElseThrow(AccountNotFoundException::new);

        if (Boolean.FALSE.equals(account.getActive())){
            log.warn("Tentativa de desativar conta que já está inativa: {}", id);
            return;
        }

        account.setActive(false);

        accountRepository.save(account);

        log.info("Conta: {} desativada com sucesso pelo usuário {}", account.getName(), userId);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteAccount(UUID id, User user) {

        Account account = accountRepository.findByIdAndUserId(id, user.getId()).orElseThrow(AccountNotFoundException::new);

        user.removeAccount(account);

        log.warn("Deletando conta permanentemente: {} do usuário {}", account.getName(), account.getUser().getName());

        accountRepository.delete(account);

    }
}
