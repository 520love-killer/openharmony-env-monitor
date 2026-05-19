package com.example.envmonitor.service;

import com.example.envmonitor.dto.AnomalyResponse;
import com.example.envmonitor.dto.ForecastResponse;
import com.example.envmonitor.dto.StatisticsSummaryResponse;
import com.example.envmonitor.entity.SensorData;
import com.example.envmonitor.repository.SensorDataRepository;
import com.example.envmonitor.util.DataSourceUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentContextService {
    private final SensorDataService sensorDataService;
    private final SensorDataRepository sensorDataRepository;
    private final AnalyticsService analyticsService;
    private final ForecastService forecastService;
    private final AnomalyService anomalyService;

    public AgentContextService(
        SensorDataService sensorDataService,
        SensorDataRepository sensorDataRepository,
        AnalyticsService analyticsService,
        ForecastService forecastService,
        AnomalyService anomalyService
    ) {
        this.sensorDataService = sensorDataService;
        this.sensorDataRepository = sensorDataRepository;
        this.analyticsService = analyticsService;
        this.forecastService = forecastService;
        this.anomalyService = anomalyService;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "agentContextCache", key = "#source")
    public Map<String, Object> context(String source) {
        String dataSource = sensorDataService.normalizeSource(source);
        StatisticsSummaryResponse summary = analyticsService.summary(dataSource, 50);
        ForecastResponse forecast = forecastService.temperatureForecast(dataSource, 50);
        AnomalyResponse anomaly = anomalyService.detect(dataSource, 50);
        Map<String, Object> dataQuality = new LinkedHashMap<>();
        long sourceCount = DataSourceUtils.ALL.equals(dataSource)
            ? sensorDataRepository.count()
            : sensorDataRepository.countByDataSource(dataSource);
        dataQuality.put("source", dataSource);
        dataQuality.put("sampleCount", sourceCount);
        dataQuality.put("mockRatio", mockRatio(dataSource));
        dataQuality.put("hasEnoughRealData", DataSourceUtils.isReal(dataSource) && sourceCount >= 5);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("latestData", sensorDataService.latestBySource(dataSource).orElse(null));
        result.put("statisticsSummary", summary);
        result.put("forecast", forecast);
        result.put("anomalies", anomaly.items());
        result.put("dataQuality", dataQuality);
        return result;
    }

    private double mockRatio(String source) {
        long total = DataSourceUtils.ALL.equals(source) ? sensorDataRepository.count() : sensorDataRepository.countByDataSource(source);
        if (total == 0) {
            return 0.0;
        }
        long mock = DataSourceUtils.ALL.equals(source) ? sensorDataRepository.countByDataSource(DataSourceUtils.MOCK)
            : DataSourceUtils.MOCK.equals(source) ? total : 0;
        return Math.round((mock * 1.0 / total) * 1000.0) / 1000.0;
    }
}
