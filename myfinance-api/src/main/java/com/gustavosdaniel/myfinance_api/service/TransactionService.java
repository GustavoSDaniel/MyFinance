package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.domain.dto.request.TransactionRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.response.TransactionResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.TransactionSearchFilter;
import com.gustavosdaniel.myfinance_api.domain.enuns.CategoryType;
import com.gustavosdaniel.myfinance_api.domain.mapping.TransactionMapper;
import com.gustavosdaniel.myfinance_api.domain.po.Category;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.domain.po.Transaction;
import com.gustavosdaniel.myfinance_api.exception.*;
import com.gustavosdaniel.myfinance_api.repository.CategoryRepository;
import com.gustavosdaniel.myfinance_api.controller.metrics.TransactionMetrics;
import com.gustavosdaniel.myfinance_api.repository.AccountRepository;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.exception.InsufficientBalanceException;
import com.gustavosdaniel.myfinance_api.repository.TransactionRepository;
import com.gustavosdaniel.myfinance_api.repository.TransactionSpecification;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionStatus;
import com.gustavosdaniel.myfinance_api.domain.enuns.TransactionType;
import com.gustavosdaniel.myfinance_api.domain.dto.request.TransferRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável por encapsular as regras de negócio relacionadas às transações financeiras.
 * Gerencia a criação, confirmação, cancelamento, transferência entre contas, exclusão e listagem
 * de transações, incluindo verificações de saldo e mecanismos de idempotência para evitar duplicidade.
 */
@Service
@CacheConfig(cacheNames = "transactions")
public class TransactionService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;
    private final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionMetrics transactionMetrics;

    public TransactionService(CategoryRepository categoryRepository, TransactionRepository transactionRepository, TransactionMapper transactionMapper, AccountRepository accountRepository, TransactionMetrics transactionMetrics) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.accountRepository = accountRepository;
        this.transactionMetrics = transactionMetrics;
    }

    /**
     * Cria uma nova transação financeira vinculada a uma conta e categoria.
     * Possui proteção contra requisições duplicadas através da chave de idempotência.
     * Limpa o cache de transações.
     *
     * @param user    Entidade do usuário que está realizando a transação.
     * @param request DTO contendo os dados da transação (valor, conta, categoria e chave de idempotência).
     * @return DTO com os detalhes da transação recém-criada.
     * @throws IdempotencyKeyException      Caso a chave de idempotência já tenha sido processada para este usuário.
     * @throws AccountNotFoundException     Caso a conta especificada não seja encontrada.
     * @throws CategoryNotFoundException    Caso a categoria especificada não seja encontrada.
     * @throws InvalidAmountException       Caso o valor da transação seja inválido.
     * @throws InsufficientBalanceException Caso a conta não tenha saldo suficiente (se for uma despesa).
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public TransactionResponse createTransaction(
            User user,
            TransactionRequest request){

        log.info("Criando transação para User {}", user.getName());

        assertIdempotencyKeyIsUnique(request, user.getId());

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

        transactionMetrics.incrementCreated();

        log.info("Transação criada com sucesso");

        return transactionMapper.toTransactionResponse(transactionSave);
    }

    /**
     * Confirma uma transação pendente, efetivando a alteração de saldo na conta.
     * Se a transação já estiver confirmada, ignora a ação.
     * Limpa o cache de transações.
     *
     * @param id     ID da transação a ser confirmada.
     * @param userId ID do usuário proprietário da transação.
     * @throws TransactionNotFoundException Caso a transação não seja encontrada.
     * @throws InvalidAmountException       Caso o valor processado seja inválido.
     * @throws InsufficientBalanceException Caso não haja saldo para confirmar a despesa.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void transactionConfirmed(UUID id, UUID userId){

        log.info("Processo de confirmação da transação: {}", id);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(TransactionNotFoundException::new);

        if (transaction.getStatus() == TransactionStatus.CONFIRMADA){

            log.warn("Transação {} já estáva confirmada ", id);
            return;
        }

        transaction.process();

        transactionRepository.save(transaction);
        accountRepository.save(transaction.getAccount());

        transactionMetrics.incrementConfirm();

        log.info("Transação: {} confirmada com sucesso", id);
    }

    /**
     * Cancela uma transação, revertendo possíveis impactos no saldo da conta.
     * Limpa o cache de transações.
     *
     * @param id     ID da transação a ser cancelada.
     * @param userId ID do usuário proprietário da transação.
     * @throws TransactionNotFoundException Caso a transação não seja encontrada.
     * @throws InvalidAmountException       Caso o estorno gere um valor inválido.
     * @throws InsufficientBalanceException Caso a conta não suporte a reversão (ex: estorno de receita que deixa o saldo negativo).
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void transactionCancel(UUID id, UUID userId) {

        log.info("Processo de cancelamento de transação: {}", id);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId).orElseThrow(TransactionNotFoundException::new);

        transaction.cancel();

        transactionRepository.save(transaction);
        accountRepository.save(transaction.getAccount());

        transactionMetrics.incrementCancel();

        log.info("Transaction {} canceled with sucesso", id);

    }

    /**
     * Realiza uma transferência de valores entre duas contas do mesmo usuário.
     * O processo cria automaticamente duas transações: uma DESPESA na conta de origem e
     * uma RECEITA na conta de destino.
     * <p>
     * A categoria utilizada para as transações é a categoria com o nome "Transferência".
     * Caso essa categoria não exista para o usuário, uma categoria padrão é criada
     * automaticamente (nome "Transferência", tipo {@link CategoryType#TRANSFERENCIA},
     * cor "#8E44AD").
     * </p>
     * Limpa o cache de transações.
     *
     * @param user            Entidade do usuário que está realizando a transferência.
     * @param transferRequest DTO contendo contas de origem/destino, valor, categoria e chave de idempotência.
     * @throws IdempotencyKeyException           Caso a transferência já tenha sido executada.
     * @throws AccountNotFoundException          Caso a conta de origem ou destino não seja encontrada.
     * @throws TransactionEqualsAccountException Caso a conta de origem seja a mesma de destino.
     * @throws InvalidAmountException            Caso o valor a ser transferido seja inválido.
     * @throws InsufficientBalanceException      Caso a conta de origem não tenha saldo suficiente.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void transfer(User user, TransferRequest transferRequest){

        log.info("Iniciando transferência da conta: {} para a conta: {}",
                transferRequest.fromAccountId(), transferRequest.toAccountId());

        assertIdempotencyKeyIsUnique(transferRequest, user.getId());

        Account fromAccount = accountRepository.findByIdAndUserId(
                transferRequest.fromAccountId(), user.getId())
                .orElseThrow(AccountNotFoundException::new);

        Account toAccount = accountRepository.findByIdAndUserId(
                transferRequest.toAccountId(), user.getId())
                .orElseThrow(AccountNotFoundException::new);

        assertDifferentAccounts(fromAccount.getId(), toAccount.getId());

        Category category = categoryRepository.findByNameAndUserId
                ("Transferência", user.getId())
                .orElseGet(() -> createDefaultTransferCategory(user));


        Transaction from = transactionMapper
                .toTransfer(transferRequest,
                        user, fromAccount, category, TransactionType.DESPESA);

        Transaction to = transactionMapper
                .toTransfer(transferRequest,
                        user, toAccount, category, TransactionType.RECEITA);

        from.process();
        to.process();

        transactionRepository.saveAll(List.of(from,to));
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        transactionMetrics.incrementTransfer();

        log.info("Transferência realizada com sucesso: {} -> {} valor: {}",
                fromAccount.getName(), toAccount.getName(), transferRequest.amount());
    }

    /**
     * Busca os detalhes de uma transação específica pelo seu ID e a valida com o ID do usuário dono.
     * O resultado desta operação é armazenado em cache.
     *
     * @param id     ID da transação a ser buscada.
     * @param userId ID do usuário proprietário.
     * @return DTO com os detalhes da transação.
     * @throws TransactionNotFoundException Caso a transação não seja encontrada.
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "{#id, #userId}")
    public TransactionResponse getTransactionById(UUID id, UUID userId) {

        log.info("Buscando transação pelo id: {}", id);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId).orElseThrow(TransactionNotFoundException::new);

        log.info("Transação: {} encontrada com sucesso", id);

        return transactionMapper.toTransactionResponse(transaction);
    }

    /**
     * Retorna uma lista paginada de transações com base em filtros dinâmicos utilizando Specifications.
     * Os filtros disponíveis incluem período, status, categoria, entre outros, conforme definido em {@link TransactionSearchFilter}.
     *
     * @param user     Entidade do usuário proprietário das transações.
     * @param filter   Objeto contendo os critérios de busca (ex: período, status, categoria).
     * @param pageable Configurações de paginação e ordenação (número da página, tamanho, ordenação).
     * @return Página contendo os DTOs das transações que correspondem aos filtros informados.
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllWithFilter(User user,
                                                      TransactionSearchFilter filter,
                                                      Pageable pageable)
    {
        log.info("Buscando todas as transações ");

        Specification<Transaction> specification = TransactionSpecification
                .filters(user.getId(), filter);

        Page<Transaction> transactions = transactionRepository.findAll(specification, pageable);

        if (transactions.isEmpty()){
            log.warn("Nenhuma transação encontrada");
            return Page.empty();
        }

        log.info("Total de transações encontrada {} transações", transactions.getTotalElements());

        return transactions.map(transactionMapper::toTransactionResponse);
    }

    /**
     * Exclui uma transação permanentemente da base de dados.
     * A exclusão é negada caso a transação já esteja confirmada, respeitando a integridade contábil.
     * Limpa o cache de transações.
     *
     * @param id     ID da transação a ser excluída.
     * @param userId ID do usuário proprietário.
     * @throws TransactionNotFoundException Caso a transação não seja encontrada.
     * @throws BusinessRuleException        Caso haja uma tentativa de excluir uma transação já confirmada.
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteTransaction(UUID id, UUID userId) {

        log.warn("Deletando transação: {}", id);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId).orElseThrow(TransactionNotFoundException::new);

        if (transaction.getStatus() == TransactionStatus.CONFIRMADA) throw new BusinessRuleException();

        transactionRepository.delete(transaction);

        transactionMetrics.incrementDeleted();

        log.info("Transação: {} deletada com sucesso", id);
    }

    /**
     * Verifica se a chave de idempotência já foi utilizada em uma transação do tipo {@link TransactionRequest}.
     * Utilizada na criação de transações comuns.
     *
     * @param request DTO contendo a chave de idempotência.
     * @param userId  ID do usuário.
     * @throws IdempotencyKeyException Se a chave já existir.
     */
    private void assertIdempotencyKeyIsUnique(TransactionRequest request, UUID userId){

        if (transactionRepository.existsByIdempotencyKeyAndUserId(request.idempotencyKey(),
                userId)){

            log.warn("Transação já processada anteriormente. idempotencyKey = {}",
                    request.idempotencyKey());

            throw new IdempotencyKeyException();
        }

    }

    /**
     * Verifica se a chave de idempotência já foi utilizada em uma transferência ({@link TransferRequest}).
     * Utilizada na criação de transferências entre contas.
     *
     * @param request DTO contendo a chave de idempotência.
     * @param userId  ID do usuário.
     * @throws IdempotencyKeyException Se a chave já existir.
     */
    private void assertIdempotencyKeyIsUnique(TransferRequest request, UUID userId){

        if (transactionRepository.existsByIdempotencyKeyAndUserId(request.idempotencyKey(),
                userId)){

            log.warn("Transação já processada anteriormente. idempotencyKey = {}",
                    request.idempotencyKey());

            throw new IdempotencyKeyException();
        }

    }

    /**
     * Verifica se as contas de origem e destino são diferentes.
     * Utilizada em transferências para evitar movimentação para a mesma conta.
     *
     * @param fromAccountId ID da conta de origem.
     * @param toAccountId   ID da conta de destino.
     * @throws TransactionEqualsAccountException Se os IDs forem iguais.
     */
    private void assertDifferentAccounts(UUID fromAccountId, UUID toAccountId){

        if (fromAccountId.equals(toAccountId))
            throw new TransactionEqualsAccountException();
    }

    /**
     * Cria e persiste uma categoria padrão para transferências entre contas.
     * <p>
     * Esta categoria é utilizada automaticamente quando uma transferência é realizada
     * e nenhuma categoria com o nome "Transferência" existe para o usuário.
     * </p>
     * <p>
     * A categoria criada possui:
     * <ul>
     *   <li>Nome: "Transferência"</li>
     *   <li>Tipo: {@link CategoryType#TRANSFERENCIA}</li>
     *   <li>Cor: "#8E44AD" (roxo)</li>
     * </ul>
     * </p>
     *
     * @param user o usuário proprietário da categoria
     * @return a entidade {@link Category} recém-criada e persistida
     */
    private Category createDefaultTransferCategory(User user){

        Category category = new Category();

        category.setUser(user);
        category.setName("Transferência");
        category.setType(CategoryType.TRANSFERENCIA);
        category.setColor("#8E44AD");

        return categoryRepository.save(category);
    }

}
