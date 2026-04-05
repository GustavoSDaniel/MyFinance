package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.domain.dto.request.AccountUpdateRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.AccountRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.response.AccountResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.enuns.Status;
import com.gustavosdaniel.myfinance_api.domain.mapping.AccountMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.exception.AccountNameDuplicateException;
import com.gustavosdaniel.myfinance_api.exception.AccountNotFoundException;
import com.gustavosdaniel.myfinance_api.controller.metrics.AccountMetrics;
import com.gustavosdaniel.myfinance_api.repository.AccountRepository;
import com.gustavosdaniel.myfinance_api.domain.po.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável por encapsular as regras de negócio relacionadas ao gerenciamento de contas.
 * Gerencia a criação, atualização, busca, alteração de status e exclusão de contas,
 * com suporte a cache para otimização de consultas.
 */
@Service
@CacheConfig(cacheNames = "accounts")
public class AccountService {

    private final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountMetrics accountMetrics;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper, AccountMetrics accountMetrics) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.accountMetrics = accountMetrics;
    }

    /**
     * Cria uma nova conta e a vincula ao usuário informado.
     * Limpa o cache de contas após a criação.
     *
     * @param accountRequest DTO contendo os dados para criação da nova conta.
     * @param user           Entidade do usuário logado que será o dono da conta.
     * @return DTO contendo as informações da conta recém-criada.
     * @throws AccountNameDuplicateException Caso o usuário já possua uma conta com o mesmo nome.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public AccountResponse createAccount(AccountRequest accountRequest, User user)
    {

        assertAccountNameIsUnique(accountRequest.name(), user.getId());

        log.info("Criando uma nova conta para o usuário: {}", user.getName());

        Account newAccount = accountMapper.toAccount(user, accountRequest);

        Account savedAccount = accountRepository.save(newAccount);

        accountMetrics.incrementCreate();

        user.addAccount(newAccount);

        log.info("Nova conta: {} adicionada para o usuário: {}", newAccount.getName(), user.getName());

        return accountMapper.toAccountResponse(savedAccount);
    }

    /**
     * Retorna uma lista de contas de um usuário com base no status solicitado.
     * O resultado desta operação é armazenado em cache.
     *
     * @param userId ID do usuário dono das contas.
     * @param status Status para filtro ("active", "disabled" ou qualquer outro valor para buscar todas).
     *               O valor é tratado de forma case-insensitive.
     * @return Lista de DTOs com as informações das contas encontradas.
     */
    @Transactional(readOnly = true)
    public List<AccountResponseInfo> getAllAccounts(UUID userId, String status) {

        log.info("Buscando todas as contas do usuário: {} com status: {}", userId, status);

        Status accountStatus = Status.fromString(status);

        List<Account> accounts =  switch (accountStatus) {

            case ACTIVE -> accountRepository.findByUserIdAndIsActiveTrue(userId);

            case DISABLED -> accountRepository.findByUserIdAndIsActiveFalse(userId);

            case ALL -> accountRepository.findByUserId(userId);

        };

        log.info("Total de contas encontradas: {}", accounts.size());

        return accounts.stream().map(accountMapper::toAccountResponseInfo).toList();
    }

    /**
     * Busca os detalhes de uma conta específica pelo seu ID e ID do dono.
     * O resultado desta operação é armazenado em cache.
     *
     * @param id     ID da conta a ser buscada.
     * @param userId ID do usuário dono da conta.
     * @return DTO com as informações detalhadas da conta.
     * @throws AccountNotFoundException Caso a conta não exista ou não pertença ao usuário.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "accounts", key = "#id + '_' + #userId")
    public AccountResponseInfo getById(UUID id, UUID userId){

        log.info("Buscando conta {} para o usuário {}", id, userId);

        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(AccountNotFoundException::new);

        log.info("Conta encontrada com sucesso");

        return accountMapper.toAccountResponseInfo(account);
    }

    /**
     * Realiza a busca de contas pelo nome para um usuário específico.
     * A busca é feita de forma case-insensitive, retornando contas cujo nome contenha
     * o valor informado (busca parcial).
     *
     * @param name   Parte ou nome completo da conta a ser pesquisada.
     * @param userId ID do usuário dono das contas.
     * @return Lista de DTOs correspondentes aos resultados da busca.
     */
    @Transactional(readOnly = true)
    public List<AccountResponseInfo> searchAccount(String name, UUID userId) {

        log.info("Buscando contas pelo nome");

        List<Account> accounts = accountRepository.searchByName(name, userId);

        log.info("Contas encontradas com sucesso {}", accounts.size());

        return accounts.stream().map(accountMapper::toAccountResponseInfo).toList();
    }

    /**
     * Atualiza os dados de uma conta existente.
     * Limpa o cache de contas após a atualização.
     *
     * @param id      ID da conta a ser atualizada.
     * @param userId  ID do usuário dono da conta.
     * @param request DTO contendo os novos dados da conta.
     * @return DTO com as informações atualizadas da conta.
     * @throws AccountNotFoundException Caso a conta não exista.
     * @throws AccountNameDuplicateException     Caso o novo nome solicitado já pertença a outra conta do usuário.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public AccountResponseInfo updateAccount(UUID id, UUID userId, AccountUpdateRequest request){

        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(AccountNotFoundException::new);

        log.info("Atualizando informações da conta: {}", account.getName());

        assertAccountNameIsUnique(request.name(), account, userId);

        accountMapper.updateAccountFromRequest(request, account);

        Account accountUpdated = accountRepository.save(account);

        accountMetrics.incrementUpdate();

        log.info("Conta: {} atualizada com sucesso", accountUpdated.getName());

        return accountMapper.toAccountResponseInfo(accountUpdated);
    }

    /**
     * Altera o status de uma conta para ativa.
     * Ignora a requisição e apenas registra um aviso caso a conta já esteja ativa.
     * Limpa o cache de contas após a alteração.
     *
     * @param id     ID da conta a ser ativada.
     * @param userId ID do usuário dono da conta.
     * @throws AccountNotFoundException Caso a conta não exista.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void activateAccount(UUID id, UUID userId) {

        log.info("Ativando conta com id: {}", id);

        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(AccountNotFoundException::new);

        if (Boolean.TRUE.equals(account.getActive())){
            log.warn("Tentativa de ativar conta que já está ativa: {}", id);
            return;
        }

        account.setActive(true);

        accountRepository.save(account);

        log.info("Conta: {} ativada com sucesso pelo usuário {}", account.getName(), userId);
    }

    /**
     * Altera o status de uma conta para inativa.
     * Ignora a requisição e apenas registra um aviso caso a conta já esteja inativa.
     * Limpa o cache de contas após a alteração.
     *
     * @param id     ID da conta a ser desativada.
     * @param userId ID do usuário dono da conta.
     * @throws AccountNotFoundException Caso a conta não exista.
     */
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

    /**
     * Remove permanentemente uma conta da base de dados e a desvincula da entidade do usuário.
     * Limpa o cache de contas após a exclusão.
     *
     * @param id   ID da conta a ser excluída.
     * @param user Entidade do usuário dono da conta, necessária para remover o vínculo na memória.
     * @throws AccountNotFoundException Caso a conta não exista.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteAccount(UUID id, User user) {

        Account account = accountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(AccountNotFoundException::new);

        user.removeAccount(account);

        log.warn("Deletando conta permanentemente: {} do usuário {}",
                account.getName(), user.getId());

        accountRepository.delete(account);

        accountMetrics.incrementDelete();

    }

    /**
     * Verifica se já existe uma conta ativa com o mesmo nome para o usuário.
     * Utilizada na criação de uma nova conta.
     *
     * @param name   Nome da conta a ser validado.
     * @param userId ID do usuário dono da conta.
     * @throws AccountNameDuplicateException Se o nome já estiver em uso.
     */
    private void assertAccountNameIsUnique(String name, UUID userId){


        if (accountRepository.existsByNameIgnoreCaseAndUserId(name, userId))

            throw new AccountNameDuplicateException();

    }

    /**
     * Verifica se o novo nome informado é único, ignorando a própria conta que está sendo atualizada.
     * Utilizada na atualização de conta.
     *
     * @param name    Novo nome proposto (pode ser null, indicando que não será alterado).
     * @param account Conta que está sendo atualizada.
     * @param userId  ID do usuário dono da conta.
     * @throws AccountNameDuplicateException Se o novo nome (não nulo e diferente do atual) já estiver em uso.
     */
    private void assertAccountNameIsUnique(String name, Account account, UUID userId){

        if (name != null && !account.getName().equalsIgnoreCase(name)){

            if (accountRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(
                    name, userId, account.getId()))

                throw new AccountNameDuplicateException();

        }
    }

}
