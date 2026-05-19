package com.example.envmonitor.service;

import com.example.envmonitor.util.DataSourceUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnomalyScheduleService {
    private final AnalyticsService analyticsService;
    private final AnomalyService anomalyService;
    private final CacheInvalidationService cacheInvalidationService;

    public AnomalyScheduleService(
        AnalyticsService analyticsService,
        AnomalyService anomalyService,
        CacheInvalidationService cacheInvalidationService
    ) {
        this.analyticsService = analyticsService;
        this.anomalyService = anomalyService;
        this.cacheInvalidationService = cacheInvalidationService;
    }

    @Transactional
    @Scheduled(cron = "${app.analytics.anomaly-cron:30 */1 * * * ?}")
    public void detectAnomalies() {
        String source = chooseRealSource();
        if (source == null) {
            System.out.println("[AnomalySchedule] real data is insufficient, anomaly detection skipped");
            return;
        }
        int saved = anomalyService.saveDetectedAnomalies(source, 50);
        cacheInvalidationService.clearAnomalyCaches();
        if (saved > 0) {
            System.out.println("[AnomalySchedule] anomaly saved count=" + saved + ", source=" + source);
        }
    }

    private String chooseRealSource() {
        if (analyticsService.chronological(DataSourceUtils.REAL_SERIAL, 50).size() >= 5) {
            return DataSourceUtils.REAL_SERIAL;
        }
        if (analyticsService.chronological(DataSourceUtils.REAL_MQTT, 50).size() >= 5) {
            return DataSourceUtils.REAL_MQTT;
        }
        return null;
    }
}
