package com.example.envmonitor.service;

import com.example.envmonitor.config.CacheConfig;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheInvalidationService {
    private final CacheManager cacheManager;

    public CacheInvalidationService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void clearAllDataCaches() {
        CacheConfig.CACHE_NAMES.forEach(this::clear);
    }

    public void clearAnalyticsCaches() {
        clear("analyticsSummaryCache");
        clear("agentContextCache");
        clear("databaseStatusCache");
    }

    public void clearAnomalyCaches() {
        clear("anomalyCache");
        clear("agentContextCache");
        clear("databaseStatusCache");
    }

    private void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
