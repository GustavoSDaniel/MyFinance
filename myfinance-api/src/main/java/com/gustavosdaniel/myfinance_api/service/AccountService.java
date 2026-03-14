package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.domain.dto.AccountRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.dto.AccountUpdateRequest;
import com.gustavosdaniel.myfinance_api.domain.mapping.AccountMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.exception.AccountAlreadyActiveException;
import com.gustavosdaniel.myfinance_api.exception.AccountAlreadyDeactivateException;
import com.gustavosdaniel.myfinance_api.exception.AccountNameDuplicateException;
import com.gustavosdaniel.myfinance_api.exception.AccountNotFoundException;
import com.gustavosdaniel.myfinance_api.repository.AccountRepository;
import com.gustavosdaniel.myfinance_api.domain.po.User;

import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * Serviço responsável pelas regras de negócio relacionadas às contas do usuário.
 *
 * <p>Esta classe gerencia operações como:
 * <ul>
 *     <li>Criação de contas</li>
 *     <li>Consulta e busca de contas</li>
 *     <li>Atualização de dados</li>
 *     <li>Ativação e desativação</li>
 *     <li>Remoção de contas</li>
 * </ul>
 *
 * <p>Todas as operações são vinculadas ao usuário autenticado.
 * Também utiliza cache para melhorar a performance das consultas.
 */
@Service
@CacheConfig(cacheNames = "accounts")
public class AccountService {

    private final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AuthHelper authHelper;

    BiPredicate<Account, AccountUpdateRequest> nameChanged =
            (account, request) ->
                    request.name() != null &&
                            !account.getName().equalsIgnoreCase(request.name());




    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper, AuthHelper authHelper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.authHelper = authHelper;
    }


    /**
     * Cria uma nova conta para o usuário autenticado.
     *
     * <p>Antes de criar a conta é verificado se já existe uma conta com o
     * mesmo nome para o usuário. Caso exista, uma exceção é lançada.
     *
     * @param accountRequest dados da conta a ser criada
     * @param principal usuário autenticado via OAuth2
     * @return resposta contendo a conta criada e a URI do recurso
     * @throws AccountNameDuplicateException caso já exista uma conta com o mesmo nome
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<AccountResponse> createAccount(AccountRequest accountRequest,
                                                         OAuth2User principal) {

        User user = authHelper.getCurrentUser(principal);

        if (accountRepository.existsByNameIgnoreCaseAndUserId(accountRequest.name().trim(),
                user.getId())){

            throw new AccountNameDuplicateException();
        }

        log.info("Criando uma nova conta para o usuário: {}", user.getName());

        Account newAccount = accountMapper.toAccount(user, accountRequest);

        Account savedAccount = accountRepository.save(newAccount);

        user.addAccount(newAccount);

        log.info("Nova conta: {} adicionada para o usuário: {}", newAccount.getName(),
                user.getName());

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedAccount.getId())
                .toUri();

        return ResponseEntity.created(uri).body(accountMapper.toAccountResponse(savedAccount));
    }

    /**
     * Retorna todas as contas do usuário autenticado.
     *
     * <p>É possível filtrar as contas pelo status:
     * <ul>
     *     <li>active - contas ativas</li>
     *     <li>disabled - contas desativadas</li>
     *     <li>null ou vazio - todas as contas</li>
     * </ul>
     *
     * @param principal usuário autenticado
     * @param status filtro de status das contas
     * @return lista de contas encontradas
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "#principal.name + '_' + #status")
    public ResponseEntity<List<AccountResponseInfo>> getAllAccounts(OAuth2User principal, String status) {

        User user = authHelper.getCurrentUser(principal);

        log.info("Buscando todas as contas do usuário: {} com status: {}", user.getName(), status);

        List<Account> accounts;

        if ("active".equalsIgnoreCase(status))
            accounts = accountRepository.findByUserIdAndIsActiveTrue(user.getId());
         else if ("disabled".equalsIgnoreCase(status))
            accounts = accountRepository.findByUserIdAndIsActiveFalse(user.getId());
         else
            accounts = accountRepository.findByUserId(user.getId());

        log.info("Total de contas encontradas: {}", accounts.size());

        return ResponseEntity.ok(
                accounts.stream()
                        .map(accountMapper::toAccountResponseInfo)
                        .toList());
    }

    /**
     * Busca uma conta específica do usuário autenticado pelo ID.
     *
     * @param id identificador da conta
     * @param principal usuário autenticado
     * @return dados da conta encontrada
     * @throws AccountNotFoundException caso a conta não exista para o usuário
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "#id + '_' + #principal.name")
    public ResponseEntity<AccountResponseInfo> getById(UUID id, OAuth2User principal){

        User user = authHelper.getCurrentUser(principal);

        log.info("Buscando conta {} para o usuário {}", id, user.getId());

        Account account = accountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(AccountNotFoundException::new);

        log.info("Conta encontrada com sucesso");

        return ResponseEntity.ok(accountMapper.toAccountResponseInfo(account));
    }

    /**
     * Realiza busca de contas pelo nome.
     *
     * <p>A busca é limitada apenas às contas do usuário autenticado.
     *
     * @param name nome ou parte do nome da conta
     * @param principal usuário autenticado
     * @return lista de contas que correspondem ao critério de busca
     */
    @Transactional(readOnly = true)
    public ResponseEntity<List<AccountResponseInfo>> searchAccount(
            String name, OAuth2User principal) {

        log.info("Buscando contas pelo nome");

        User user = authHelper.getCurrentUser(principal);

        List<Account> accounts = accountRepository.searchByName(name, user.getId());

        log.info("Contas encontradas com sucesso {}", accounts.size());

        return ResponseEntity.ok(
                accounts.stream()
                        .map(accountMapper::toAccountResponseInfo)
                        .toList());
    }

    /**
     * Atualiza os dados de uma conta existente.
     *
     * <p>Se o nome da conta for alterado, é verificado se já existe outra conta
     * com o mesmo nome para o usuário.
     *
     * @param id identificador da conta
     * @param principal usuário autenticado
     * @param request novos dados da conta
     * @return conta atualizada
     * @throws AccountNotFoundException caso a conta não exista
     * @throws AccountNameDuplicateException caso já exista outra conta com o mesmo nome
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<AccountResponseInfo> updateAccount(
            UUID id, OAuth2User principal, AccountUpdateRequest request){

        User user = authHelper.getCurrentUser(principal);

        Account account = accountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(AccountNotFoundException::new);

        log.info("Atualizando informações da conta: {}", account.getName());

        if (nameChanged.test(account, request)){

            if (accountRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(request.name(),
                    user.getId(), id)){

                    throw new AccountNameDuplicateException();
            }
        }

        accountMapper.updateAccountFromRequest(request, account);

        Account accountUpdated = accountRepository.save(account);

        log.info("Conta: {} atualizada com sucesso", accountUpdated.getName());

        return ResponseEntity.ok(accountMapper.toAccountResponseInfo(accountUpdated));
    }

    /**
     * Ativa uma conta que está desativada.
     *
     * @param id identificador da conta
     * @param principal usuário autenticado
     * @return resposta indicando sucesso na operação
     * @throws AccountNotFoundException caso a conta não exista
     * @throws AccountAlreadyActiveException caso a conta já esteja ativa
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> activateAccount(UUID id, OAuth2User principal) {

        log.info("Ativando conta com id: {}", id);

        User user = authHelper.getCurrentUser(principal);

        Account account = accountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(AccountNotFoundException::new);

        if (Boolean.TRUE.equals(account.getActive())){

            log.warn("Tentativa de ativar conta que já está ativa: {}", id);

            throw new AccountAlreadyActiveException();
        }

        account.setActive(true);

        accountRepository.save(account);

        log.info("Conta: {} ativada com sucesso pelo usuário {}", account.getName(),
                user.getId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Desativa uma conta ativa.
     *
     * @param id identificador da conta
     * @param principal usuário autenticado
     * @return resposta indicando sucesso na operação
     * @throws AccountNotFoundException caso a conta não exista
     * @throws AccountAlreadyDeactivateException caso a conta já esteja desativada
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deactivateAccount(UUID id, OAuth2User principal) {

        log.info("Desativando conta com id: {}", id);

        User user = authHelper.getCurrentUser(principal);

        Account account = accountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(AccountNotFoundException::new);

        if (Boolean.FALSE.equals(account.getActive())){

            log.warn("Tentativa de desativar conta que já está inativa: {}", id);

            throw new AccountAlreadyDeactivateException();
        }

        account.setActive(false);

        accountRepository.save(account);

        log.info("Conta: {} desativada com sucesso pelo usuário {}",
                account.getName(), user.getId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Remove permanentemente uma conta do usuário.
     *
     * <p>A conta só pode ser removida pelo próprio usuário dono da conta.
     *
     * @param id identificador da conta
     * @param principal usuário autenticado
     * @return resposta indicando sucesso na remoção
     * @throws AccountNotFoundException caso a conta não exista
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deleteAccount(UUID id, OAuth2User principal) {

        User user = authHelper.getCurrentUser(principal);

        Account account = accountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(AccountNotFoundException::new);

        log.warn("Deletando conta permanentemente: {} do usuário {}",
                account.getName(), account.getUser().getName());

        user.removeAccount(account);

        accountRepository.delete(account);

        return ResponseEntity.noContent().build();

    }
}
