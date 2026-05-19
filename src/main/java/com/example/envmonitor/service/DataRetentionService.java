package com.example.envmonitor.service;

import com.example.envmonitor.repository.AnalyticsSummaryRepository;
import com.example.envmonitor.repository.AnomalyRecordRepository;
import com.example.envmonitor.repository.SensorDataRepository;
import com.example.envmonitor.util.DataSourceUtils;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataRetentionService {
    private final SensorDataRepository sensorDataRepository;
    private final AnalyticsSummaryRepository analyticsSummaryRepository;
    private final AnomalyRecordRepository anomalyRecordRepository;
    private final CacheInvalidationService cacheInvalidationService;
    private final int rawDays;
    private final int summaryDays;
    private final int anomalyDays;

    public DataRetentionService(
        SensorDataRepository sensorDataRepository,
        AnalyticsSummaryRepository analyticsSummaryRepository,
        AnomalyRecordRepository anomalyRecordRepository,
        CacheInvalidationService cacheInvalidationService,
        @Value("${app.retention.raw-days:30}") int rawDays,
        @Value("${app.retention.summary-days:180}") int summaryDays,
        @Value("${app.retention.anomaly-days:180}") int anomalyDays
    ) {
        this.sensorDataRepository = sensorDataRepository;
        this.analyticsSummaryRepository = analyticsSummaryRepository;
        this.anomalyRecordRepository = anomalyRecordRepository;
        this.cacheInvalidationService = cacheInvalidationService;
        this.rawDays = rawDays;
        this.summaryDays = summaryDays;
        this.anomalyDays = anomalyDays;
    }

    @Transactional
    @Scheduled(cron = "${app.analytics.cleanup-cron:0 0 3 * * ?}")
    public void cleanupExpiredData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rawCutoff = now.minusDays(Math.max(rawDays, 1));
        LocalDateTime mockCutoff = now.minusDays(7);
        LocalDateTime summaryCutoff = now.minusDays(Math.max(summaryDays, 1));
        LocalDateTime anomalyCutoff = now.minusDays(Math.max(anomalyDays, 1));

        System.out.println("[DataRetention] cleanup start rawCutoff=" + rawCutoff
            + ", mockCutoff=" + mockCutoff + ", summaryCutoff=" + summaryCutoff + ", anomalyCutoff=" + anomalyCutoff);

        int realDeleted = sensorDataRepository.deleteByDataSourceInAndCreatedAtBefore(
            List.of(DataSourceUtils.REAL_SERIAL, DataSourceUtils.REAL_MQTT, DataSourceUtils.MANUAL), rawCutoff);
        int mockDeleted = sensorDataRepository.deleteByDataSourceAndCreatedAtBefore(DataSourceUtils.MOCK, mockCutoff);
        int summaryDeleted = analyticsSummaryRepository.deleteByCreatedAtBefore(summaryCutoff);
        int anomalyDeleted = anomalyRecordRepository.deleteByCreatedAtBefore(anomalyCutoff);

        cacheInvalidationService.clearAllDataCaches();
        System.out.println("[DataRetention] cleanup done realDeleted=" + realDeleted
            + ", mockDeleted=" + mockDeleted + ", summaryDeleted=" + summaryDeleted + ", anomalyDeleted=" + anomalyDeleted);
    }
}
