package com.gustavosdaniel.myfinance_api.metrics;

import com.gustavosdaniel.myfinance_api.util.MetricsBuilder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Componente que encapsula as métricas relacionadas à entidade {@code User}.
 * <p>
 * Utiliza o {@link MetricsBuilder} para criar e registrar contadores ({@link Counter})
 * e temporizadores
 * ({@link Timer}) que monitoram operações de criação, exclusão e consultas de usuários.
 * Fornece métodos convenientes para incrementar os contadores e medir a latência das operações via
 * {@link Timer#record(Supplier)}.
 * </p>
 * <p>
 * As métricas registradas são:
 * <ul>
 *   <li><b>user.created</b> – número de usuários criados</li>
 *   <li><b>user.delete</b> – número de usuários deletados</li>
 *   <li><b>current.user</b> – latência da operação de obtenção do usuário atual (current user)</li>
 *   <li><b>user.get.all</b> – latência da listagem de todos os usuários</li>
 *   <li><b>user.get.by.id</b> – latência da busca de usuário por ID</li>
 *   <li><b>user.get.by.email</b> – latência da busca de usuário por e-mail</li>
 * </ul>
 * </p>
 */
@Component
public class UserMetrics {

    private final Counter counterCreated;
    private final Counter counterDeleted;
    private final Timer currentTImer;
    private final Timer getAllTimer;
    private final Timer getByIdTimer;
    private final Timer getByEmail;

    public UserMetrics(MetricsBuilder metricsBuilder){

        this.counterCreated = metricsBuilder.counter("user.created",
                "Usuário criado");
        this.counterDeleted = metricsBuilder.counter("user.delete",
                "Usuário deletado");
        this.currentTImer = metricsBuilder.timer("current.user",
                "Latencia currentUser");
        this.getAllTimer = metricsBuilder.timer("user.get.all",
                "latencia getAllUser");
        this.getByIdTimer = metricsBuilder.timer("user.get.by.id",
                "latencia getById");
        this.getByEmail = metricsBuilder.timer("user.get.by.email",
                "latencia getByEmail");

    }

    public void incrementCreated(){counterCreated.increment();}
    public void incrementDeleted(){counterDeleted.increment();}

    public <T> T recordCurrent(Supplier<T> supplier){return currentTImer.record(supplier);}
    public <T> T recordGetAll(Supplier<T> supplier){return getAllTimer.record(supplier);}
    public <T> T recordGetById(Supplier<T> supplier){return getByIdTimer.record(supplier);}
    public <T> T recordGetByEmail(Supplier<T> supplier){return getByEmail.record(supplier);}

}
