package com.gustavosdaniel.myfinance_api.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Componente responsável pela construção e registro de métricas no sistema.
 * <p>
 * Esta classe encapsula a criação de objetos de métrica do Micrometer, como {@link Counter} e {@link Timer},
 * utilizando o {@link MeterRegistry} injetado. Fornece métodos convenientes que aplicam configurações padrão,
 * como descrições e, para timers, publicação de histograma e percentis pré-definidos (50%, 95% e 99%).
 * </p>
 * <p>
 * É um bean Spring ({@link Component}) e deve ser injetado onde for necessário registrar métricas personalizadas.
 * </p>
 *
 * @author Gustavo Daniel
 * @version 1.0
 * @see io.micrometer.core.instrument.MeterRegistry
 * @see io.micrometer.core.instrument.Counter
 * @see io.micrometer.core.instrument.Timer
 */
@Component
public class MetricsBuilder {

    private final MeterRegistry registry;

    /**
     * Construtor que recebe o {@link MeterRegistry} do Micrometer.
     *
     * @param registry O registro de métricas onde os contadores e timers serão armazenados.
     */
    public MetricsBuilder(MeterRegistry registry){

        this.registry = registry;
    }

    /**
     * Cria e registra um contador ({@link Counter}) com o nome e descrição fornecidos.
     * <p>
     * O contador é registrado no {@link MeterRegistry} injetado e pode ser utilizado para medir
     * eventos que ocorrem de forma incremental (ex.: número de usuários criados).
     * </p>
     *
     * @param name        Nome do contador. Deve seguir as convenções do Micrometer (ex.: "api.requests.total").
     * @param description Descrição textual do contador, explicando o que ele mede.
     * @return Uma instância de {@link Counter} registrada e pronta para uso.
     */
    public Counter counter(String name, String description){

        return Counter.builder(name)
                .description(description)
                .register(this.registry);
    }


    /**
     * Cria e registra um temporizador ({@link Timer}) com o nome e descrição fornecidos.
     * <p>
     * O timer é configurado automaticamente para:
     * <ul>
     *   <li>Publicar histograma percentil ({@code .publishPercentileHistogram()})</li>
     *   <li>Publicar percentis específicos: 50%, 95% e 99% ({@code .publishPercentiles(0.5, 0.95, 0.99)})</li>
     * </ul>
     * Essas configurações permitem analisar a distribuição dos tempos de execução de operações
     * (ex.: duração de chamadas a APIs, consultas ao banco de dados, etc.).
     * </p>
     *
     * @param name        Nome do timer. Deve seguir as convenções do Micrometer (ex.: "api.request.duration").
     * @param description Descrição textual do timer, explicando o que ele mede.
     * @return Uma instância de {@link Timer} registrada e configurada com as opções padrão.
     */
    public Timer timer(String name, String description){

        return Timer.builder(name)
                .description(description)
                .publishPercentileHistogram()
                .publishPercentiles(0.5,0.95,0.99)
                .register(this.registry);
    }
}
