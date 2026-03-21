package com.gustavosdaniel.myfinance_api.controller.metrics;

import com.gustavosdaniel.myfinance_api.util.MetricsBuilder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Componente que encapsula as métricas relacionadas à entidade {@code Account}.
 * <p>
 * Esta classe utiliza o {@link MetricsBuilder} para criar e registrar contadores ({@link Counter})
 * e temporizadores ({@link Timer}) que monitoram operações de criação, atualização,
 * exclusão e consultas
 * de contas. Fornece métodos convenientes para incrementar os contadores e medir a
 * latência de operações
 * via {@link Timer#record(Supplier)}.
 * </p>
 * <p>
 * As métricas registradas são:
 * <ul>
 *   <li><b>accounts.created</b> – número de contas criadas</li>
 *   <li><b>accounts.update</b> – número de atualizações realizadas</li>
 *   <li><b>account.delete</b> – número de exclusões realizadas</li>
 *   <li><b>accounts.get.by.id</b> – latência da busca de conta por ID</li>
 *   <li><b>account.get.all</b> – latência da listagem de todas as contas</li>
 *   <li><b>account.get.search</b> – latência da busca por critérios</li>
 * </ul>
 * </p>
 */
@Component
public class AccountMetrics {

    private final Counter createCounter;
    private final Counter updateCounter;
    private final Counter deleteCounter;
    private final Timer getByIdTimer;
    private final Timer getAllTimer;
    private final Timer searchTimer;


    /**
     * Construtor que recebe um {@link MetricsBuilder} e inicializa todas as métricas.
     * <p>
     * Cada métrica é criada através do {@code MetricsBuilder} com um nome e uma descrição
     * Os contadores medem a frequência das operações; os timers medem a latência
     * e são configurados automaticamente com percentis (50%, 95%, 99%).
     * </p>
     *
     * @param metricsBuilder Builder responsável por criar e registrar as métricas no
     * {@link io.micrometer.core.instrument.MeterRegistry}.
     */
    public AccountMetrics(MetricsBuilder metricsBuilder){

        this.createCounter = metricsBuilder.counter("accounts.created",
                "Criou uma nova conta");

        this.updateCounter = metricsBuilder.counter("accounts.update",
                "Atualizou a conta");

        this.deleteCounter = metricsBuilder.counter("account.delete",
                "Deletou a conta");

        this.getByIdTimer = metricsBuilder.timer("accounts.get.by.id",
                "Latencia de getById");

        this.getAllTimer = metricsBuilder.timer("account.get.all",
                "Latencia de getAll");

        this.searchTimer = metricsBuilder.timer("account.get.search",
                "Latencia de search");
    }

    public void incrementCreate(){createCounter.increment();}
    public void incrementUpdate(){updateCounter.increment();}
    public void incrementDelete(){deleteCounter.increment();}

    public <T> T recordGetById(Supplier<T> supplier){return getByIdTimer.record(supplier);}
    public <T> T recordGetAll(Supplier<T> supplier){return getAllTimer.record(supplier);}
    public <T> T recordGetSearch(Supplier<T> supplier){return searchTimer.record(supplier);}

}
