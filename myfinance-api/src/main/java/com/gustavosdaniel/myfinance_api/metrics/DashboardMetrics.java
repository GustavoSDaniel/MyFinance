package com.gustavosdaniel.myfinance_api.metrics;

import com.gustavosdaniel.myfinance_api.util.MetricsBuilder;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Componente que encapsula as métricas relacionadas ao dashboard.
 * <p>
 * Utiliza o {@link MetricsBuilder} para criar e registrar um temporizador ({@link Timer})
 * que monitora a latência da operação de obtenção de dados para o dashboard.
 * Fornece um método conveniente para medir o tempo de execução da operação via
 * {@link Timer#record(Supplier)}.
 * </p>
 * <p>
 * A métrica registrada é:
 * <ul>
 *   <li><b>dashboard</b> – latência da operação de carregamento do dashboard</li>
 * </ul>
 * </p>
 */
@Component
public class DashboardMetrics {

    private final Timer getDashboard;

    public DashboardMetrics (MetricsBuilder metricsBuilder){

        this.getDashboard = metricsBuilder.timer("dashboard",
                "get.deashboard");
    }

    public <T> T dashboard(Supplier<T> supplier){return getDashboard.record(supplier);}

}
