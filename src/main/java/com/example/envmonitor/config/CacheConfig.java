package com.example.envmonitor.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    public static final List<String> CACHE_NAMES = List.of(
        "latestSensorDataCache",
        "recentSensorDataCache",
        "analyticsSummaryCache",
        "analyticsTrendCache",
        "forecastCache",
        "anomalyCache",
        "agentContextCache",
        "agentToolResultCache",
        "agentKnowledgeCache",
        "databaseStatusCache",
        "scenarioCache"
    );

    @Bean
    public CacheManager cacheManager(
        @Value("${app.cache.ttl-seconds:30}") long ttlSeconds,
        @Value("${app.cache.max-size:1000}") long maxSize
    ) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheNames(CACHE_NAMES);
        manager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
            .maximumSize(maxSize)
            .recordStats());
        return manager;
    }
}
