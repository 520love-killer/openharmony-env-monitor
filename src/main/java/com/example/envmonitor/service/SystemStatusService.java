package com.example.envmonitor.service;

import com.example.envmonitor.config.CacheConfig;
import com.example.envmonitor.entity.AnalyticsSummary;
import com.example.envmonitor.entity.AnomalyRecord;
import com.example.envmonitor.entity.SensorData;
import com.example.envmonitor.repository.AnalyticsSummaryRepository;
import com.example.envmonitor.repository.AnomalyRecordRepository;
import com.example.envmonitor.repository.SensorDataRepository;
import com.example.envmonitor.util.DataSourceUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemStatusService {
    private final SensorDataRepository sensorDataRepository;
    private final AnalyticsSummaryRepository analyticsSummaryRepository;
    private final AnomalyRecordRepository anomalyRecordRepository;
    private final CacheManager cacheManager;
    private final SerialIngestionService serialIngestionService;
    private final String datasourceUrl;
    private final int rawDays;
    private final int summaryDays;
    private final int anomalyDays;
    private final String cleanupCron;
    private final String cacheProvider;
    private final long cacheTtlSeconds;
    private final long cacheMaxSize;

    public SystemStatusService(
        SensorDataRepository sensorDataRepository,
        AnalyticsSummaryRepository analyticsSummaryRepository,
        AnomalyRecordRepository anomalyRecordRepository,
        CacheManager cacheManager,
        SerialIngestionService serialIngestionService,
        @Value("${spring.datasource.url:}") String datasourceUrl,
        @Value("${app.retention.raw-days:30}") int rawDays,
        @Value("${app.retention.summary-days:180}") int summaryDays,
        @Value("${app.retention.anomaly-days:180}") int anomalyDays,
        @Value("${app.analytics.cleanup-cron:0 0 3 * * ?}") String cleanupCron,
        @Value("${app.cache.provider:caffeine}") String cacheProvider,
        @Value("${app.cache.ttl-seconds:30}") long cacheTtlSeconds,
        @Value("${app.cache.max-size:1000}") long cacheMaxSize
    ) {
        this.sensorDataRepository = sensorDataRepository;
        this.analyticsSummaryRepository = analyticsSummaryRepository;
        this.anomalyRecordRepository = anomalyRecordRepository;
        this.cacheManager = cacheManager;
        this.serialIngestionService = serialIngestionService;
        this.datasourceUrl = datasourceUrl;
        this.rawDays = rawDays;
        this.summaryDays = summaryDays;
        this.anomalyDays = anomalyDays;
        this.cleanupCron = cleanupCron;
        this.cacheProvider = cacheProvider;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.cacheMaxSize = cacheMaxSize;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "databaseStatusCache", key = "'database'")
    public Map<String, Object> databaseStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("database", databaseName());
        result.put("connected", true);
        result.put("sensorDataCount", sensorDataRepository.count());
        result.put("realSerialCount", sensorDataRepository.countByDataSource(DataSourceUtils.REAL_SERIAL));
        result.put("realMqttCount", sensorDataRepository.countByDataSource(DataSourceUtils.REAL_MQTT));
        result.put("mockCount", sensorDataRepository.countByDataSource(DataSourceUtils.MOCK));
        result.put("manualCount", sensorDataRepository.countByDataSource(DataSourceUtils.MANUAL));
        result.put("latestDataTime", sensorDataRepository.findTopByOrderByCreatedAtDesc().map(SensorData::getCreatedAt).orElse(null));
        result.put("latestSummaryTime", analyticsSummaryRepository.findTopByOrderByCreatedAtDesc().map(AnalyticsSummary::getCreatedAt).orElse(null));
        result.put("latestAnomalyTime", anomalyRecordRepository.findTopByOrderByCreatedAtDesc().map(AnomalyRecord::getCreatedAt).orElse(null));
        return result;
    }

    public Map<String, Object> retentionPolicy() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rawDataRetentionDays", rawDays);
        result.put("mockDataRetentionDays", 7);
        result.put("summaryRetentionDays", summaryDays);
        result.put("anomalyRetentionDays", anomalyDays);
        result.put("cleanupCron", cleanupCron);
        result.put("description", "每天凌晨 3:00 自动清理过期数据");
        return result;
    }

    public Map<String, Object> cacheStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", cacheProvider);
        result.put("enabled", cacheManager != null);
        result.put("ttlSeconds", cacheTtlSeconds);
        result.put("maxSize", cacheMaxSize);
        result.put("cacheNames", CacheConfig.CACHE_NAMES);
        result.put("description", "Caffeine local cache is enabled for dashboard analytics APIs");
        return result;
    }

    public Map<String, Object> serialStatus() {
        return serialIngestionService.status();
    }

    private String databaseName() {
        String urlWithoutQuery = datasourceUrl;
        int query = datasourceUrl.indexOf('?');
        if (query >= 0) {
            urlWithoutQuery = datasourceUrl.substring(0, query);
        }
        int slash = urlWithoutQuery.lastIndexOf('/');
        if (slash < 0) {
            return "unknown";
        }
        String name = urlWithoutQuery.substring(slash + 1);
        return name.isBlank() ? "unknown" : name;
    }
}
