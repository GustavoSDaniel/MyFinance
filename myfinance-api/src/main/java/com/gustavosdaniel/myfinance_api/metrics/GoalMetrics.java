package com.gustavosdaniel.myfinance_api.metrics;

import com.gustavosdaniel.myfinance_api.util.MetricsBuilder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Componente que encapsula as métricas relacionadas à entidade {@code Goal}.
 * <p>
 * Utiliza o {@link MetricsBuilder} para criar e registrar contadores ({@link Counter})
 * e temporizadores
 * ({@link Timer}) que monitoram operações de criação, atualização, exclusão e consultas de metas.
 * Fornece métodos convenientes para incrementar os contadores e medir a latência das operações via
 * {@link Timer#record(Supplier)}.
 * </p>
 * <p>
 * As métricas registradas são:
 * <ul>
 *   <li><b>goal.created</b> – número de metas criadas</li>
 *   <li><b>goal.update</b> – número de atualizações realizadas</li>
 *   <li><b>goal.deleted</b> – número de exclusões realizadas</li>
 *   <li><b>get.all.goal</b> – latência da listagem de todas as metas</li>
 *   <li><b>get.by.id.goal</b> – latência da busca de meta por ID</li>
 *   <li><b>search.name</b> – latência da busca por nome de meta</li>
 * </ul>
 * </p>
 */
@Component
public class GoalMetrics {

    private final Counter counterCreated;
    private final Counter counterUpdate;
    private final Counter counterDeleted;

    private final Timer getAllTimer;
    private final Timer getByIdTimer;
    private final Timer searchNameTimer;

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
    public GoalMetrics(MetricsBuilder metricsBuilder){

        this.counterCreated = metricsBuilder.counter("goal.created",
                "Meta criada");
        this.counterUpdate = metricsBuilder.counter("goal.update",
                "Atualizou meta");
        this.counterDeleted = metricsBuilder.counter("goal.deleted",
                "Meta deletada");
        this.getAllTimer = metricsBuilder.timer("get.all.goal",
                "Latencia getAll");
        this.getByIdTimer = metricsBuilder.timer("get.by.id.goal",
                "Latencia getById");
        this.searchNameTimer = metricsBuilder.timer("search.name",
                "Latencia searchName");

    }

    public void incrementCreated(){counterCreated.increment();}
    public void incrementUpdate(){counterUpdate.increment();}
    public void incrementDelete(){counterDeleted.increment();}

    public <T> T recordGetAll(Supplier<T> supplier){return getAllTimer.record(supplier);}
    public <T> T recordGetById(Supplier<T> supplier){return getByIdTimer.record(supplier);}
    public <T> T recordSearchName(Supplier<T> supplier){return searchNameTimer.record(supplier);}

}
