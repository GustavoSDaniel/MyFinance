package com.gustavosdaniel.myfinance_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(45))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string())

                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json())
                );

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("accounts", config.entryTtl(Duration.ofSeconds(accountsTtl)));

        cacheConfigurations.put("categories", config.entryTtl(Duration.ofSeconds(categoriesTtl)));

        cacheConfigurations.put("users", config.entryTtl(Duration.ofSeconds(usersTtl)));

        cacheConfigurations.put("dashboards", config.entryTtl(Duration.ofSeconds(dashboardsTtl)));

        cacheConfigurations.put("goals", config.entryTtl(Duration.ofSeconds(goalsTtl)));

        cacheConfigurations.put("transactions", config.entryTtl(Duration.ofSeconds(transactionsTtl)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

}
