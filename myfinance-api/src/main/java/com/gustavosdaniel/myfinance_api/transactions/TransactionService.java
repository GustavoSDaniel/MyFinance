package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.accounts.*;
import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.exception.*;
import com.gustavosdaniel.myfinance_api.categories.CategoryRepository;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.exception.InsufficientBalanceException;
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

@Service
@CacheConfig(cacheNames = "transactions")
public class TransactionService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;
    private final Logger log = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(CategoryRepository categoryRepository, TransactionRepository transactionRepository, TransactionMapper transactionMapper, AccountRepository accountRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.accountRepository = accountRepository;
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public TransactionResponse createTransaction(
            User user,
            TransactionRequest request) throws InvalidAmountException, InsufficientBalanceException {

        log.info("Criando transação");

        if (transactionRepository.existsByIdempotencyKeyAndUserId(request.idempotencyKey(), user.getId())){

            log.warn("Transação já processada anteriormente. idempotencyKey = {}", request.idempotencyKey());

            throw new IdempotencyKeyException();
        }

        Account account = accountRepository
                .findByIdAndUserId(request.accountId(), user.getId()).orElseThrow(AccountNotFoundException::new);

        Category category = categoryRepository
                .findByIdAndUserId(request.categoryId(), user.getId()).orElseThrow(CategoryNotFoundException::new);

        Transaction transaction = transactionMapper
                .toTransaction(request,user, account, category);

        transaction.process();

        Transaction transactionSave = transactionRepository.save(transaction);
        accountRepository.save(transaction.getAccount());

        log.info("Transação criada com sucesso");

        return transactionMapper.toTransactionResponse(transactionSave);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void transactionConfirmed(UUID id, UUID userId) throws InvalidAmountException, InsufficientBalanceException {

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

        log.info("Transação: {} confirmada com sucesso", id);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void transactionCancel(UUID id, UUID userId) throws InvalidAmountException, InsufficientBalanceException {

        log.info("Processo de cancelamento de transação: {}", id);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId).orElseThrow(TransactionNotFoundException::new);

        transaction.cancel();

        transactionRepository.save(transaction);
        accountRepository.save(transaction.getAccount());

        log.info("Transaction {} canceled with sucesso", id);

    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void transfer(User user, TransferRequest transferRequest) throws InvalidAmountException, InsufficientBalanceException {

        log.info("Iniciando transferência da conta: {} para a conta: {}",
                transferRequest.fromAccountId(), transferRequest.toAccountId());

        if (transactionRepository.existsByIdempotencyKeyAndUserId(
                transferRequest.idempotencyKey(), user.getId())){

            log.warn("Transferência já processada anteriormente. idempotencyKey = {}",
                    transferRequest.idempotencyKey());

            throw  new IdempotencyKeyException();
        }

        Account fromAccount = accountRepository.findByIdAndUserId(
                transferRequest.fromAccountId(), user.getId()).orElseThrow(AccountNotFoundException::new);

        Account toAccount = accountRepository.findByIdAndUserId(
                transferRequest.toAccountId(), user.getId()).orElseThrow(AccountNotFoundException::new);

        Category category = categoryRepository.findByIdAndUserId
                (transferRequest.categoryId(), user.getId()).orElseThrow(CategoryNotFoundException::new);

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
    }


    @Transactional(readOnly = true)
    @Cacheable(key = "{#id, #userId}")
    public TransactionResponse getTransactionById(UUID id, UUID userId) {

        log.info("Buscando transação pelo id: {}", id);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId).orElseThrow(TransactionNotFoundException::new);

        log.info("Transação: {} encontrada com sucesso", id);

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllWithFilter(User user, TransactionSearchFilter filter, Pageable pageable)
    {
        log.info("Buscando todas as transações ");

        Specification<Transaction> specification = TransactionSpecification.filters(user.getId(), filter);

        Page<Transaction> transactions = transactionRepository.findAll(specification, pageable);

        if (transactions.isEmpty()){
            log.warn("Nenhuma transação encontrada");
            return Page.empty();
        }

        log.info("Total de transações encontrada {} transações", transactions.getTotalElements());

        return transactions.map(transactionMapper::toTransactionResponse);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteTransaction(UUID id, UUID userId) {

        log.warn("Deletando transação: {}", id);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId).orElseThrow(TransactionNotFoundException::new);

        if (transaction.getStatus() == TransactionStatus.CONFIRMADA){
            throw new BusinessRuleException();
        }

        transactionRepository.delete(transaction);

        log.info("Transação: {} deletada com sucesso", id);
    }

}
