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
 * Configuração do cache da aplicação utilizando DragonFlyDb.
 * <p>
 * Define as configurações padrão de cache, como serialização JSON, desativação de armazenamento
 * de valores nulos e tempo de vida (TTL) padrão de 45 minutos. Permite configurações específicas
 * para cada região de cache (accounts, categories, users, dashboards, goals, transactions)
 * através das propriedades do application.yml/application.properties.
 * </p>
 * <p>
 * Os tempos de vida (TTL) para cada região são configuráveis via:
 * <ul>
 *   <li>{@code app.cache.ttl.accounts}</li>
 *   <li>{@code app.cache.ttl.categories}</li>
 *   <li>{@code app.cache.ttl.users}</li>
 *   <li>{@code app.cache.ttl.dashboards}</li>
 *   <li>{@code app.cache.ttl.goals}</li>
 *   <li>{@code app.cache.ttl.transactions}</li>
 * </ul>
 * </p>
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
