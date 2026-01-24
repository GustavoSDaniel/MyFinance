package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.user.User;

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

    public AccountServiceImpl(AccountRepository accountRepository, AccountMapper accountMapper ) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }


    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest accountRequest, User user) throws AccountNameDuplicate {

        if (accountRepository.existsByNameIgnoreCaseAndUserId(accountRequest.name().trim(), user.getId())){

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

        accountRepository.save(account);

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

        accountRepository.save(account);

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
