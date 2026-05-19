package com.example.envmonitor.service;

import com.example.envmonitor.dto.AnomalyResponse;
import com.example.envmonitor.dto.ForecastResponse;
import com.example.envmonitor.dto.ScenarioAnalysisResponse;
import com.example.envmonitor.dto.ScenarioAnalysisResponse.ScenarioAdviceItem;
import com.example.envmonitor.dto.ScenarioAnalysisResponse.ScenarioMetricStatus;
import com.example.envmonitor.dto.ScenarioAnalysisResponse.ScenarioRiskItem;
import com.example.envmonitor.dto.ScenarioAnalysisResponse.ScenarioScoreBreakdown;
import com.example.envmonitor.dto.StatisticsSummaryResponse;
import com.example.envmonitor.entity.SensorData;
import com.example.envmonitor.service.ScenarioRuleEngine.Thresholds;
import com.example.envmonitor.util.DataSourceUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScenarioAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(ScenarioAnalysisService.class);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SensorDataService sensorDataService;
    private final AnalyticsService analyticsService;
    private final ForecastService forecastService;
    private final AnomalyService anomalyService;
    private final ScenarioProfileService scenarioProfileService;

    public ScenarioAnalysisService(
        SensorDataService sensorDataService,
        AnalyticsService analyticsService,
        ForecastService forecastService,
        AnomalyService anomalyService,
        ScenarioProfileService scenarioProfileService
    ) {
        this.sensorDataService = sensorDataService;
        this.analyticsService = analyticsService;
        this.forecastService = forecastService;
        this.anomalyService = anomalyService;
        this.scenarioProfileService = scenarioProfileService;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "scenarioCache", key = "#scenario + '_' + #userRole + '_' + #source + '_' + #limit + '_' + #crop + '_' + #roomType")
    public ScenarioAnalysisResponse analyze(
        String scenario, String userRole, String source, int limit,
        String crop, String roomType,
        Double customTempMin, Double customTempMax,
        Double customHumidityMin, Double customHumidityMax, Double customGasMax
    ) {
        String normalizedSource = sensorDataService.normalizeSource(source);
        boolean isReal = DataSourceUtils.isReal(normalizedSource);
        boolean isMock = DataSourceUtils.MOCK.equals(normalizedSource);

        List<SensorData> data = sensorDataService.recentBySource(normalizedSource, limit);

        if (data.isEmpty()) {
            return emptyResponse(scenario, userRole, normalizedSource, isMock);
        }

        if (data.size() < 5) {
            return insufficientDataResponse(scenario, userRole, normalizedSource, data.size(), isMock);
        }

        // Get aggregated data
        StatisticsSummaryResponse summary = analyticsService.summary(normalizedSource, limit);
        ForecastResponse forecast = forecastService.temperatureForecast(normalizedSource, limit);
        AnomalyResponse anomaly = anomalyService.detect(normalizedSource, limit);

        SensorData latest = data.get(0);
        double temp = latest.getTemperature();
        double humidity = latest.getHumidity();
        double gas = latest.getGas();

        // Calculate trends from recent data
        double tempTrend = calculateTrend(data, SensorData::getTemperature);
        double humidityTrend = calculateTrend(data, SensorData::getHumidity);

        // Get thresholds
        Thresholds t = ScenarioRuleEngine.thresholds(
            scenario, crop, roomType,
            customTempMin, customTempMax, customHumidityMin, customHumidityMax, customGasMax
        );

        // Evaluate risks
        List<ScenarioRiskItem> risks = scenarioProfileService.evaluateRisks(
            scenario, t, temp, humidity, gas, tempTrend, humidityTrend
        );

        // Calculate scores
        ScenarioScoreBreakdown breakdown = calculateScoreBreakdown(
            scenario, t, temp, humidity, gas,
            tempTrend, summary, anomaly
        );

        int score = calculateWeightedScore(scenario, breakdown);
        score = Math.max(0, Math.min(100, score));

        String level = ScenarioRuleEngine.levelFromScore(score);
        String levelName = ScenarioRuleEngine.levelNameFromScore(score);

        // Generate advices
        List<ScenarioAdviceItem> advices = scenarioProfileService.generateAdvices(
            scenario, t, temp, humidity, gas, risks
        );

        // Build metrics status
        String tempStatus = tempStatus(temp, t);
        String humidityStatus = humidityStatus(humidity, t);
        String gasStatus = gas >= 50 ? "WARNING" : gas >= 80 ? "CRITICAL" : "SAFE";
        String trendStatus = trendStatus(tempTrend);

        // Generate summary
        String summaryText = scenarioProfileService.generateSummary(
            scenario, score, levelName, risks, data.size()
        );

        // Algorithm notes
        List<String> algorithmNotes = buildAlgorithmNotes();

        // Confidence
        String confidence = data.size() >= 50 ? "HIGH" : data.size() >= 20 ? "MEDIUM" : "LOW";
        if (!isReal) confidence = "LOW";

        return new ScenarioAnalysisResponse(
            scenario,
            ScenarioRuleEngine.scenarioName(scenario),
            userRole,
            ScenarioRuleEngine.roleName(userRole),
            score,
            level,
            levelName,
            confidence,
            summaryText,
            risks,
            advices,
            new ScenarioMetricStatus(tempStatus, humidityStatus, gasStatus, trendStatus),
            breakdown,
            algorithmNotes,
            normalizedSource,
            data.size(),
            LocalDateTime.now().format(DT_FMT)
        );
    }

    private ScenarioAnalysisResponse emptyResponse(String scenario, String userRole, String source, boolean isMock) {
        String prefix = isMock ? "【模拟数据】" : "";
        return new ScenarioAnalysisResponse(
            scenario, ScenarioRuleEngine.scenarioName(scenario),
            userRole, ScenarioRuleEngine.roleName(userRole),
            0, "WARNING", "预警", "LOW",
            prefix + "当前没有可用数据，无法进行场景分析。",
            List.of(), List.of(),
            new ScenarioMetricStatus("UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN"),
            new ScenarioScoreBreakdown(0, 0, 0, 0, 0),
            buildAlgorithmNotes(), source, 0, LocalDateTime.now().format(DT_FMT)
        );
    }

    private ScenarioAnalysisResponse insufficientDataResponse(String scenario, String userRole, String source, int count, boolean isMock) {
        String prefix = isMock ? "【模拟数据】" : "";
        return new ScenarioAnalysisResponse(
            scenario, ScenarioRuleEngine.scenarioName(scenario),
            userRole, ScenarioRuleEngine.roleName(userRole),
            0, "WARNING", "预警", "LOW",
            prefix + "当前只有 " + count + " 条数据，不足以进行可靠分析，建议等待更多数据采集。",
            List.of(), List.of(),
            new ScenarioMetricStatus("UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN"),
            new ScenarioScoreBreakdown(0, 0, 0, 0, 0),
            buildAlgorithmNotes(), source, count, LocalDateTime.now().format(DT_FMT)
        );
    }

    private ScenarioScoreBreakdown calculateScoreBreakdown(
        String scenario, Thresholds t,
        double temp, double humidity, double gas,
        double tempTrend, StatisticsSummaryResponse summary, AnomalyResponse anomaly
    ) {
        // Temperature score: 100 when within range, linearly decreases outside
        int tempScore = scoreWithinRange(temp, t.tempMin(), t.tempMax(), 10.0);

        // Humidity score
        int humidityScore = scoreWithinRange(humidity, t.humidityMin(), t.humidityMax(), 15.0);

        // Gas score: 100 when < 30, linearly decreases to 0 at 100
        int gasScore = gas < 30 ? 100 : (int) Math.max(0, 100 - (gas - 30) * 100 / 70);

        // Trend score: stable = 100, fast changing = lower
        int trendScore = Math.max(0, 100 - (int) (Math.abs(tempTrend) * 100));

        // Anomaly score: fewer anomalies = higher
        int anomalyScore = anomaly != null && anomaly.success()
            ? Math.max(0, 100 - anomaly.anomalyCount() * 20)
            : 80;

        return new ScenarioScoreBreakdown(tempScore, humidityScore, gasScore, trendScore, anomalyScore);
    }

    private int scoreWithinRange(double value, double min, double max, double penaltyRange) {
        if (value >= min && value <= max) {
            // Within ideal range: calculate how close to center
            double center = (min + max) / 2.0;
            double halfRange = (max - min) / 2.0;
            if (halfRange <= 0) return 100;
            double distance = Math.abs(value - center);
            // Perfect at center, decreases slightly towards edges
            return (int) Math.max(70, 100 - (distance / halfRange) * 15);
        }
        // Outside range: penalize based on distance
        double distance;
        if (value < min) {
            distance = min - value;
        } else {
            distance = value - max;
        }
        return (int) Math.max(0, 80 - distance * (80.0 / penaltyRange));
    }

    private int calculateWeightedScore(String scenario, ScenarioScoreBreakdown b) {
        double[] w = ScenarioRuleEngine.weights(scenario);
        return (int) Math.round(
            b.temperatureScore() * w[0]
                + b.humidityScore() * w[1]
                + b.gasScore() * w[2]
                + b.trendScore() * w[3]
                + b.anomalyScore() * w[4]
        );
    }

    private String tempStatus(double temp, Thresholds t) {
        if (temp >= t.tempMin() && temp <= t.tempMax()) return "NORMAL";
        if (temp > t.tempMax() + 5 || temp < t.tempMin() - 5) return "CRITICAL";
        return "WARNING";
    }

    private String humidityStatus(double humidity, Thresholds t) {
        if (humidity >= t.humidityMin() && humidity <= t.humidityMax()) return "NORMAL";
        if (humidity > t.humidityMax() + 10 || humidity < t.humidityMin() - 10) return "CRITICAL";
        return "WARNING";
    }

    private String trendStatus(double trend) {
        if (Math.abs(trend) < 0.1) return "STABLE";
        if (Math.abs(trend) < 0.3) return "SLIGHT_CHANGE";
        return trend > 0 ? "RISING" : "FALLING";
    }

    private double calculateTrend(List<SensorData> data, java.util.function.Function<SensorData, Double> extractor) {
        if (data.size() < 2) return 0;
        int n = Math.min(data.size(), 10);
        double first = extractor.apply(data.get(n - 1));
        double last = extractor.apply(data.get(0));
        return (last - first) / Math.max(1, n - 1);
    }

    private List<String> buildAlgorithmNotes() {
        List<String> notes = new ArrayList<>();
        notes.add("使用滑动平均平滑短期波动，减少传感器噪声");
        notes.add("使用线性回归判断温度整体上升或下降趋势");
        notes.add("使用指数平滑让最近数据拥有更高权重");
        notes.add("使用场景化阈值进行风险判断，不同场景采用不同标准");
        notes.add("使用加权评分计算综合环境分，各场景权重不同");
        notes.add("当前模型适合趋势判断和短期预警，不是工业级精确控制模型");
        return notes;
    }
}
