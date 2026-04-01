package com.gustavosdaniel.myfinance_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuração centralizada de cache da aplicação utilizando Redis (DragonFlyDb) como armazenamento.
 * <p>
 * Esta classe configura o gerenciamento de cache do Spring, habilitando o suporte a anotações
 * como {@code @Cacheable}, {@code @CacheEvict} e {@code @CachePut}. Define um cache manager
 * baseado em Redis com serialização JSON customizada e políticas de expiração específicas
 * para cada região de cache.
 * </p>
 * <p>
 * As configurações padrão incluem:
 * <ul>
 *   <li>Tempo de vida (TTL) padrão de 45 minutos</li>
 *   <li>Desativação de armazenamento de valores nulos</li>
 *   <li>Serialização de chaves como strings (padrão)</li>
 *   <li>Serialização de valores usando {@link GenericJacksonJsonRedisSerializer} com um
 *   {@link ObjectMapper} customizado</li>
 * </ul>
 * </p>
 * <p>
 * Para cada região de cache, é possível sobrescrever o TTL via propriedades no
 * {@code application.yml} ou {@code application.properties}:
 * <pre>
 * app:
 *   cache:
 *     ttl:
 *       accounts: 3600          # 1 hora em segundos
 *       categories: 1800        # 30 minutos
 *       users: 7200             # 2 horas
 *       dashboards: 300         # 5 minutos
 *       goals: 3600             # 1 hora
 *       transactions: 1800      # 30 minutos
 * </pre>
 * </p>
 *
 * @author Gustavo Daniel
 * @version 1.0
 * @see EnableCaching
 * @see RedisCacheManager
 * @see GenericJacksonJsonRedisSerializer
 * @see RedisSerializer
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.ttl.accounts}")
    private long accountsTtl;
    @Value("${app.cache.ttl.categories}")
    private long categoriesTtl;
    @Value("${app.cache.ttl.users}")
    private long usersTtl;
    @Value("${app.cache.ttl.dashboards}")
    private long dashboardsTtl;
    @Value("${app.cache.ttl.goals}")
    private long goalsTtl;
    @Value("${app.cache.ttl.transactions}")
    private long transactionsTtl;

    /**
     * Cria e configura o {@link RedisCacheManager} responsável por gerenciar os caches da aplicação.
     * <p>
     * O cache manager é construído com uma configuração base ({@link RedisCacheConfiguration}) que
     * define o comportamento padrão. Para cada região de cache (accounts, categories, users, etc.),
     * é aplicado um TTL específico obtido das propriedades configuradas.
     * </p>
     *
     * @param redisConnectionFactory fábrica de conexões com o Redis (DragonFlyDb) injetada pelo Spring
     * @param objectMapper           {@link ObjectMapper} customizado para serialização JSON,
     *                               geralmente configurado para suportar local dates, UUIDs, etc.
     * @return uma instância configurada de {@link RedisCacheManager}
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
                                          ObjectMapper objectMapper) {

        GenericJacksonJsonRedisSerializer jsonSerializer =
                new GenericJacksonJsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(45))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(RedisSerializer.string())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer)
                );

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("accounts",     config.entryTtl(Duration.ofSeconds(accountsTtl)));
        cacheConfigurations.put("categories",   config.entryTtl(Duration.ofSeconds(categoriesTtl)));
        cacheConfigurations.put("users",        config.entryTtl(Duration.ofSeconds(usersTtl)));
        cacheConfigurations.put("dashboards",   config.entryTtl(Duration.ofSeconds(dashboardsTtl)));
        cacheConfigurations.put("goals",        config.entryTtl(Duration.ofSeconds(goalsTtl)));
        cacheConfigurations.put("transactions", config.entryTtl(Duration.ofSeconds(transactionsTtl)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}