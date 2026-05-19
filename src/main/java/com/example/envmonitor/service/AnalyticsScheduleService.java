package com.example.envmonitor.service;

import com.example.envmonitor.entity.AnalyticsSummary;
import com.example.envmonitor.entity.SensorData;
import com.example.envmonitor.repository.AnalyticsSummaryRepository;
import com.example.envmonitor.util.DataSourceUtils;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsScheduleService {
    private final AnalyticsService analyticsService;
    private final AnalyticsSummaryRepository analyticsSummaryRepository;
    private final CacheInvalidationService cacheInvalidationService;

    public AnalyticsScheduleService(
        AnalyticsService analyticsService,
        AnalyticsSummaryRepository analyticsSummaryRepository,
        CacheInvalidationService cacheInvalidationService
    ) {
        this.analyticsService = analyticsService;
        this.analyticsSummaryRepository = analyticsSummaryRepository;
        this.cacheInvalidationService = cacheInvalidationService;
    }

    @Transactional
    @Scheduled(cron = "${app.analytics.summary-cron:0 */1 * * * ?}")
    public void generateSummary() {
        String source = chooseRealSource();
        if (source == null) {
            System.out.println("[AnalyticsSchedule] real data is insufficient, summary skipped");
            return;
        }
        List<SensorData> data = analyticsService.chronological(source, 50);
        AnalyticsSummary summary = analyticsService.computeSummary(source, data);
        analyticsSummaryRepository.save(summary);
        cacheInvalidationService.clearAnalyticsCaches();
        System.out.println("[AnalyticsSchedule] summary saved, source=" + source + ", sampleCount=" + data.size());
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
