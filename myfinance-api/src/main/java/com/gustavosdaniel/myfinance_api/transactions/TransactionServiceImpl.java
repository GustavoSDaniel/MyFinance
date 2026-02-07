package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.accounts.AccountRepository;
import com.gustavosdaniel.myfinance_api.accounts.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.InsufficientBalanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService{

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;
    private final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public TransactionResponse createTransaction(
            TransactionProfile profile,
            TransactionRequest request) throws InvalidAmountException, InsufficientBalanceException {

        log.info("Criando transação");

        Transaction transaction = transactionMapper
                .toTransaction(request,profile.user(), profile.account(), profile.category());

        transaction.process();

        Transaction transactionSave = transactionRepository.save(transaction);
        accountRepository.save(transaction.getAccount());

        log.info("Transação criada com sucesso");

        return transactionMapper.toTransactionResponse(transactionSave);
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public void transactionCancel(UUID id, UUID userId) throws InvalidAmountException, InsufficientBalanceException {

        log.info("Processo de cancelamento de transação: {}", id);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId).orElseThrow(TransactionNotFoundException::new);

        transaction.cancel();

        transactionRepository.save(transaction);
        accountRepository.save(transaction.getAccount());

        log.info("Transaction {} canceled with sucesso", id);

    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id, UUID userId) {

        log.info("Buscando transação pelo id: {}", id);

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId).orElseThrow(TransactionNotFoundException::new);

        log.info("Transação: {} encontrada com sucesso", id);

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
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

        log.info("Todas as transaçÕes encontrada {} ", transactions.getSize());

        return transactions.map(transactionMapper::toTransactionResponse);
    }

    @Override
    @Transactional
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
