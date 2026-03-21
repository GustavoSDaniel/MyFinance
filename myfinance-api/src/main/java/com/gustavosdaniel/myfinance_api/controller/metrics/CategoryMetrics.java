package com.gustavosdaniel.myfinance_api.controller.metrics;

import com.gustavosdaniel.myfinance_api.util.MetricsBuilder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Componente que encapsula as métricas relacionadas à entidade {@code Category}.
 * <p>
 * Utiliza o {@link MetricsBuilder} para criar e registrar contadores ({@link Counter})
 * e temporizadores
 * ({@link Timer}) que monitoram operações de criação, atualização, exclusão e consultas
 * de categorias.
 * Fornece métodos convenientes para incrementar os contadores e medir a latência das operações via
 * {@link Timer#record(Supplier)}.
 * </p>
 * <p>
 * As métricas registradas são:
 * <ul>
 *   <li><b>category.created</b> – número de categorias criadas</li>
 *   <li><b>categories.update</b> – número de atualizações realizadas</li>
 *   <li><b>categorie.deleted</b> – número de exclusões realizadas (observação: nome com "categorie")</li>
 *   <li><b>get.all.category</b> – latência da listagem de todas as categorias</li>
 *   <li><b>search.name.category</b> – latência da busca por nome de categoria</li>
 *   <li><b>get.by.id.category</b> – latência da busca de categoria por ID</li>
 * </ul>
 * </p>
 */
@Component
public class CategoryMetrics {

    private final Counter countCreated;
    private final Counter countUpdate;
    private final Counter countDelete;

    private final Timer getAllTimer;
    private final Timer searchNameTimer;
    private final Timer getByIdTimer;

    /**
     * Construtor que recebe um {@link MetricsBuilder} e inicializa todas as métricas.
     * <p>
     * Cada métrica é criada através do {@code MetricsBuilder} com um nome e uma descrição
     * descritiva. Os contadores medem a frequência das operações; os timers medem a latência
     * e são configurados automaticamente com percentis (50%, 95%, 99%).
     * </p>
     *
     * @param metricsBuilder Builder responsável por criar e
     *registrar as métricas no {@link io.micrometer.core.instrument.MeterRegistry}.
     */
    public CategoryMetrics(MetricsBuilder metricsBuilder){

        this.countCreated = metricsBuilder.counter("category.created",
                "Criou uma categoria");

        this.countUpdate = metricsBuilder.counter("categories.update",
                "Atualizou a categoria");

        this.countDelete = metricsBuilder.counter(
                "categorie.deleted",
                "Categoria deletada");

        this.getAllTimer = metricsBuilder.timer("get.all.category",
                "Latencia getAll");

        this.searchNameTimer = metricsBuilder.timer("search.name.category",
                "Latencia search");

        this.getByIdTimer = metricsBuilder.timer("get.by.id.category",
                "Latencia getById");
    }

    public void incrementCreated(){countCreated.increment(); }
    public void incrementUpdate(){countUpdate.increment();}
    public void incrementDelete(){countDelete.increment();}

    public <T> T recordGetAll(Supplier<T> supplier){return getAllTimer.record(supplier);}
    public <T> T recordSearchName(Supplier<T> supplier){return searchNameTimer.record(supplier);}
    public <T> T recordGetById(Supplier<T> supplier){return getByIdTimer.record(supplier);}

}