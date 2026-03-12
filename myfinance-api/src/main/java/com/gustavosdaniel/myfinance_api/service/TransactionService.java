package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.domain.dto.TransactionRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.TransactionResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.TransactionSearchFilter;
import com.gustavosdaniel.myfinance_api.domain.dto.TransferRequest;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionStatus;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;
import com.gustavosdaniel.myfinance_api.domain.mapping.TransactionMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.domain.po.Transaction;
import com.gustavosdaniel.myfinance_api.exception.*;
import com.gustavosdaniel.myfinance_api.repository.CategoryRepository;
import com.gustavosdaniel.myfinance_api.repository.AccountRepository;
import com.gustavosdaniel.myfinance_api.repository.TransactionRepository;
import com.gustavosdaniel.myfinance_api.repository.TransactionSpecification;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável pelas regras de negócio relacionadas às transações financeiras.
 *
 * <p>Uma transação representa uma movimentação financeira do usuário, podendo ser:
 * <ul>
 *     <li>Receita</li>
 *     <li>Despesa</li>
 *     <li>Transferência entre contas</li>
 * </ul>
 *
 * <p>Este serviço gerencia operações como:
 * <ul>
 *     <li>Criação de transações</li>
 *     <li>Confirmação e cancelamento</li>
 *     <li>Transferências entre contas</li>
 *     <li>Busca e filtragem de transações</li>
 *     <li>Remoção de transações</li>
 * </ul>
 *
 * <p>Todas as operações são vinculadas ao usuário autenticado.
 * Algumas consultas utilizam cache para melhorar a performance.
 */
@Service
@CacheConfig(cacheNames = "transactions")
public class TransactionService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;
    private final AuthHelper authHelper;

    private final Logger log = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(CategoryRepository categoryRepository, TransactionRepository transactionRepository, TransactionMapper transactionMapper, AccountRepository accountRepository, AuthHelper authHelper) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.accountRepository = accountRepository;
        this.authHelper = authHelper;
    }

    /**
     * Cria uma nova transação financeira.
     *
     * <p>Antes de criar a transação é verificado se a {@code idempotencyKey}
     * já foi utilizada anteriormente para evitar duplicação de transações.
     *
     * <p>Após a criação, a transação é processada e o saldo da conta é atualizado.
     *
     * @param principal usuário autenticado
     * @param request dados da transação a ser criada
     * @return resposta contendo a transação criada e a URI do recurso
     * @throws IdempotencyKeyException caso a transação já tenha sido processada
     * @throws AccountNotFoundException caso a conta não exista
     * @throws CategoryNotFoundException caso a categoria não exista
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<TransactionResponse> createTransaction(
            OAuth2User principal,
            TransactionRequest request){

        log.info("Criando transação");

        User user = authHelper.getCurrentUser(principal);

        if (transactionRepository.existsByIdempotencyKeyAndUserId(request.idempotencyKey(),
                user.getId())){

            log.warn("Transação já processada anteriormente. idempotencyKey = {}",
                    request.idempotencyKey());

            throw new IdempotencyKeyException();
        }

        Account account = accountRepository
                .findByIdAndUserId(request.accountId(), user.getId())
                .orElseThrow(AccountNotFoundException::new);

        Category category = categoryRepository
                .findByIdAndUserId(request.categoryId(), user.getId())
                .orElseThrow(CategoryNotFoundException::new);

        Transaction transaction = transactionMapper
                .toTransaction(request,user, account, category);

        transaction.process();

        Transaction transactionSave = transactionRepository.save(transaction);
        accountRepository.save(transaction.getAccount());

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transactionSave.getId())
                .toUri();

        log.info("Transação criada com sucesso");

        return ResponseEntity.created(uri)
                .body(transactionMapper.toTransactionResponse(transactionSave));
    }

    /**
     * Confirma uma transação pendente.
     *
     * <p>Ao confirmar a transação, o saldo da conta é atualizado de acordo
     * com o tipo da transação (receita ou despesa).
     *
     * @param id identificador da transação
     * @param principal usuário autenticado
     * @return resposta indicando sucesso na operação
     * @throws TransactionNotFoundException caso a transação não exista
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> transactionConfirmed(UUID id, OAuth2User principal) {

        log.info("Processo de confirmação da transação: {}", id);

        User user = authHelper.getCurrentUser(principal);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(TransactionNotFoundException::new);

        if (transaction.getStatus() == TransactionStatus.CONFIRMADA){

            log.warn("Transação {} já estáva confirmada ", id);
            return ResponseEntity.noContent().build();
        }

        transaction.process();

        transactionRepository.save(transaction);
        accountRepository.save(transaction.getAccount());

        log.info("Transação: {} confirmada com sucesso", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Cancela uma transação existente.
     *
     * <p>Ao cancelar a transação, os efeitos da movimentação financeira
     * são revertidos no saldo da conta.
     *
     * @param id identificador da transação
     * @param principal usuário autenticado
     * @return resposta indicando sucesso na operação
     * @throws TransactionNotFoundException caso a transação não exista
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> transactionCancel(UUID id, OAuth2User principal){

        log.info("Processo de cancelamento de transação: {}", id);

        User user = authHelper.getCurrentUser(principal);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(TransactionNotFoundException::new);

        transaction.cancel();

        transactionRepository.save(transaction);
        accountRepository.save(transaction.getAccount());

        log.info("Transaction {} canceled with sucesso", id);

        return ResponseEntity.noContent().build();

    }

    /**
     * Realiza uma transferência entre duas contas do usuário.
     *
     * <p>A transferência é composta por duas transações:
     * <ul>
     *     <li>Uma despesa na conta de origem</li>
     *     <li>Uma receita na conta de destino</li>
     * </ul>
     *
     * <p>Também é utilizada uma {@code idempotencyKey} para evitar
     * que a transferência seja processada mais de uma vez.
     *
     * @param principal usuário autenticado
     * @param transferRequest dados da transferência
     * @return resposta indicando sucesso na operação
     * @throws IdempotencyKeyException caso a transferência já tenha sido processada
     * @throws AccountNotFoundException caso alguma conta não exista
     * @throws CategoryNotFoundException caso a categoria não exista
     * @throws TransactionEqualsAccountException caso as contas sejam iguais
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> transfer(OAuth2User principal, TransferRequest transferRequest){

        log.info("Iniciando transferência da conta: {} para a conta: {}",
                transferRequest.fromAccountId(), transferRequest.toAccountId());

        User user = authHelper.getCurrentUser(principal);

        if (transactionRepository.existsByIdempotencyKeyAndUserId(
                transferRequest.idempotencyKey(), user.getId())){

            log.warn("Transferência já processada anteriormente. idempotencyKey = {}",
                    transferRequest.idempotencyKey());

            throw  new IdempotencyKeyException();
        }

        Account fromAccount = accountRepository.findByIdAndUserId(
                transferRequest.fromAccountId(), user.getId())
                .orElseThrow(AccountNotFoundException::new);

        Account toAccount = accountRepository.findByIdAndUserId(
                transferRequest.toAccountId(), user.getId())
                .orElseThrow(AccountNotFoundException::new);

        Category category = categoryRepository.findByIdAndUserId
                (transferRequest.categoryId(), user.getId())
                .orElseThrow(CategoryNotFoundException::new);

        if (fromAccount.equals(toAccount)){

            throw new TransactionEqualsAccountException();
        }

        LocalDateTime transactionDate = transferRequest.date() != null
                ? transferRequest.date().atStartOfDay()
                : LocalDateTime.now();


        Transaction from = new Transaction(
                transferRequest.idempotencyKey(),
                user,
                fromAccount,
                category,
                transferRequest.description(),
                transferRequest.amount(),
                TransactionType.DESPESA,
                transactionDate,
                null,
                null
                );

        Transaction to = new Transaction(transferRequest.idempotencyKey(),
                user,
                toAccount,
                category,
                transferRequest.description(),
                transferRequest.amount(),
                TransactionType.RECEITA,
                transactionDate,
                null,
                null
        );

        from.process();
        to.process();

        transactionRepository.saveAll(List.of(from,to));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        log.info("Transferência realizada com sucesso: {} -> {} valor: {}",
                fromAccount.getName(), toAccount.getName(), transferRequest.amount());

        return ResponseEntity.noContent().build();
    }


    /**
     * Busca uma transação específica pelo ID.
     *
     * <p>A transação deve pertencer ao usuário autenticado.
     *
     * @param id identificador da transação
     * @param principal usuário autenticado
     * @return dados da transação encontrada
     * @throws TransactionNotFoundException caso a transação não exista
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "{#id, #principal.name}")
    public ResponseEntity<TransactionResponse> getTransactionById(UUID id, OAuth2User principal) {

        log.info("Buscando transação pelo id: {}", id);

        User user = authHelper.getCurrentUser(principal);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(TransactionNotFoundException::new);

        log.info("Transação: {} encontrada com sucesso", id);

        return ResponseEntity.ok(transactionMapper.toTransactionResponse(transaction));
    }

    /**
     * Retorna uma lista paginada de transações com filtros dinâmicos.
     *
     * <p>Os filtros podem incluir:
     * <ul>
     *     <li>Período de datas</li>
     *     <li>Conta</li>
     *     <li>Categoria</li>
     *     <li>Tipo de transação</li>
     * </ul>
     *
     * @param principal usuário autenticado
     * @param filter critérios de busca
     * @param pageable informações de paginação
     * @return página contendo as transações encontradas
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<TransactionResponse>> getAllWithFilter(
            OAuth2User principal, TransactionSearchFilter filter, Pageable pageable)
    {
        log.info("Buscando todas as transações ");

        User user = authHelper.getCurrentUser(principal);

        Specification<Transaction> specification = TransactionSpecification
                .filters(user.getId(), filter);

        Page<Transaction> transactions = transactionRepository
                .findAll(specification, pageable);

        if (transactions.isEmpty()){
            log.warn("Nenhuma transação encontrada");
            return ResponseEntity.noContent().build();
        }

        log.info("Total de transações encontrada {} transações",
                transactions.getTotalElements());

        return ResponseEntity.ok(transactions.map(transactionMapper::toTransactionResponse));
    }

    /**
     * Remove uma transação do sistema.
     *
     * <p>Transações já confirmadas não podem ser removidas
     * para preservar a consistência financeira do sistema.
     *
     * @param id identificador da transação
     * @param principal usuário autenticado
     * @return resposta indicando sucesso na remoção
     * @throws TransactionNotFoundException caso a transação não exista
     * @throws BusinessRuleException caso a transação já esteja confirmada
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deleteTransaction(UUID id, OAuth2User principal) {

        log.warn("Deletando transação: {}", id);

        User user = authHelper.getCurrentUser(principal);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(TransactionNotFoundException::new);

        if (transaction.getStatus() == TransactionStatus.CONFIRMADA){
            throw new BusinessRuleException();
        }

        transactionRepository.delete(transaction);

        log.info("Transação: {} deletada com sucesso", id);

        return ResponseEntity.noContent().build();
    }

}
