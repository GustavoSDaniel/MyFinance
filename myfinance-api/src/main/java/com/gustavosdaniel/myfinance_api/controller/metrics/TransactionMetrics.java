package com.gustavosdaniel.myfinance_api.controller.metrics;

import com.gustavosdaniel.myfinance_api.util.MetricsBuilder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Componente que encapsula as métricas relacionadas à entidade {@code Transaction}.
 * <p>
 * Utiliza o {@link MetricsBuilder} para criar e registrar contadores ({@link Counter})
 * e temporizadores
 * ({@link Timer}) que monitoram operações de criação, confirmação, cancelamento,
 * transferência, exclusão
 * e consultas de transações. Fornece métodos para incrementar os contadores
 * e medir a
 * latência das operações via {@link Timer#record(Supplier)}.
 * </p>
 * <p>
 * As métricas registradas são:
 * <ul>
 *   <li><b>transaction.created</b> – número de transações criadas</li>
 *   <li><b>transaction.confimed</b> – número de transações confirmadas (observação: descrição "Transação confirmada")</li>
 *   <li><b>transaction.cancel</b> – número de transações canceladas (observação: descrição "Transação confirmada", possivelmente erro de digitação)</li>
 *   <li><b>transaction.transfer</b> – número de transações de transferência realizadas</li>
 *   <li><b>transaction.deleted</b> – número de transações deletadas</li>
 *   <li><b>by.id.transaction</b> – latência da busca de transação por ID</li>
 *   <li><b>get.all.transaction</b> – latência da listagem de todas as transações</li>
 * </ul>
 * </p>
 */
@Component
public class TransactionMetrics {

    private final Counter createCounter;
    private final Counter confirmCounter;
    private final Counter cancelCounter;
    private final Counter transferCounter;
    private final Counter deleteCounter;
    private final Timer findByIdTimer;
    private final Timer getAllTimer;


    /**
     * Construtor que recebe um {@link MetricsBuilder} e inicializa todas as métricas.
     * <p>
     * Cada métrica é criada através do {@code MetricsBuilder} com um nome e uma descrição
     * descritiva. Os contadores medem a frequência das operações; os timers medem a latência
     * e são configurados automaticamente com percentis (50%, 95%, 99%).
     * </p>
     *
     * @param metricsBuilder Builder responsável por criar e registrar as métricas no
     * {@link io.micrometer.core.instrument.MeterRegistry}.
     */
    public TransactionMetrics(MetricsBuilder metricsBuilder){

        this.createCounter = metricsBuilder.counter("transaction.created",
                "Transação criada");
        this.confirmCounter = metricsBuilder.counter("transaction.confimed",
                "Transação confirmada");
        this.cancelCounter = metricsBuilder.counter("transaction.cancel",
                "Transação confirmada");
        this.transferCounter = metricsBuilder.counter("transaction.transfer",
                "Transações realizadas");
        this.deleteCounter = metricsBuilder.counter("transaction.deleted",
                "Transações deletadas");
        this.findByIdTimer = metricsBuilder.timer("by.id.transaction",
                "Latencia getById");
        this.getAllTimer = metricsBuilder.timer("get.all.transaction",
                "Latencia getAll");
    }

    public void incrementCreated(){createCounter.increment();}
    public void incrementConfirm(){confirmCounter.increment();}
    public void incrementCancel(){cancelCounter.increment();}
    public void incrementTransfer(){transferCounter.increment();}
    public void incrementDeleted(){deleteCounter.increment();}

    public <T> T recordGetById(Supplier<T> supplier){return findByIdTimer.record(supplier);}
    public <T> T recordGetAll(Supplier<T> supplier){return getAllTimer.record(supplier);}

}
