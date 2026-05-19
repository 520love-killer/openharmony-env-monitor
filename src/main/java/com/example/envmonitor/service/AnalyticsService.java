package com.example.envmonitor.service;

import com.example.envmonitor.dto.StatisticsSummaryResponse;
import com.example.envmonitor.dto.TrendSummaryResponse;
import com.example.envmonitor.entity.AnalyticsSummary;
import com.example.envmonitor.entity.SensorData;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private static final int MIN_REAL_SAMPLE_COUNT = 5;

    private final SensorDataService sensorDataService;

    public AnalyticsService(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analyticsSummaryCache", key = "#source + '_' + #limit")
    public StatisticsSummaryResponse summary(String source, int limit) {
        String dataSource = sensorDataService.normalizeSource(source);
        List<SensorData> data = chronological(dataSource, limit);
        if (data.size() < MIN_REAL_SAMPLE_COUNT) {
            return insufficient(dataSource, data.size(), "真实数据不足，至少需要 5 条数据");
        }
        return toResponse(computeSummary(dataSource, data), true);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analyticsTrendCache", key = "#source + '_' + #limit")
    public TrendSummaryResponse trend(String source, int limit) {
        String dataSource = sensorDataService.normalizeSource(source);
        List<SensorData> data = chronological(dataSource, limit);
        if (data.size() < MIN_REAL_SAMPLE_COUNT) {
            return new TrendSummaryResponse(false, "真实数据不足，至少需要 5 条数据", dataSource, data.size(),
                null, null, null, null, null);
        }
        AnalyticsSummary summary = computeSummary(dataSource, data);
        String tempTrend = trendByRate(summary.getTemperatureChangeRate(), 0.02);
        String humidityTrend = trendByRate(summary.getHumidityChangeRate(), 0.05);
        String gasTrend = trendByRate(summary.getGasChangeRate(), 0.1);
        String overall = "STABLE";
        if ("UP".equals(tempTrend) || "UP".equals(gasTrend) || "UP".equals(humidityTrend)) {
            overall = "UP";
        } else if ("DOWN".equals(tempTrend) || "DOWN".equals(gasTrend) || "DOWN".equals(humidityTrend)) {
            overall = "DOWN";
        }
        String explanation = String.format("温度%s，湿度%s，燃气浓度%s，整体趋势%s",
            trendText(tempTrend), trendText(humidityTrend), trendText(gasTrend), trendText(overall));
        return new TrendSummaryResponse(true, "趋势计算完成", dataSource, data.size(),
            tempTrend, humidityTrend, gasTrend, overall, explanation);
    }

    public AnalyticsSummary computeSummary(String source, List<SensorData> chronologicalData) {
        MetricStats temp = stats(chronologicalData.stream().map(SensorData::getTemperature).toList());
        MetricStats humidity = stats(chronologicalData.stream().map(SensorData::getHumidity).toList());
        MetricStats gas = stats(chronologicalData.stream().map(SensorData::getGas).toList());

        SensorData first = chronologicalData.get(0);
        SensorData last = chronologicalData.get(chronologicalData.size() - 1);
        double minutes = Math.max(1.0, Duration.between(first.getCreatedAt(), last.getCreatedAt()).toSeconds() / 60.0);
        double tempRate = (last.getTemperature() - first.getTemperature()) / minutes;
        double humidityRate = (last.getHumidity() - first.getHumidity()) / minutes;
        double gasRate = (last.getGas() - first.getGas()) / minutes;

        String volatility = volatility(temp.std(), gas.std());
        String trend = overallTrend(tempRate, humidityRate, gasRate);

        AnalyticsSummary summary = new AnalyticsSummary();
        summary.setSource(source);
        summary.setSampleCount(chronologicalData.size());
        summary.setTemperatureAvg(round2(temp.avg()));
        summary.setTemperatureMax(round2(temp.max()));
        summary.setTemperatureMin(round2(temp.min()));
        summary.setTemperatureStd(round2(temp.std()));
        summary.setHumidityAvg(round2(humidity.avg()));
        summary.setHumidityMax(round2(humidity.max()));
        summary.setHumidityMin(round2(humidity.min()));
        summary.setHumidityStd(round2(humidity.std()));
        summary.setGasAvg(round2(gas.avg()));
        summary.setGasMax(round2(gas.max()));
        summary.setGasMin(round2(gas.min()));
        summary.setGasStd(round2(gas.std()));
        summary.setTemperatureChangeRate(round3(tempRate));
        summary.setHumidityChangeRate(round3(humidityRate));
        summary.setGasChangeRate(round3(gasRate));
        summary.setVolatilityLevel(volatility);
        summary.setTrend(trend);
        summary.setMessage("最近 " + chronologicalData.size() + " 条数据整体" + trendText(trend) + "，波动程度 " + volatility);
        return summary;
    }

    public StatisticsSummaryResponse toResponse(AnalyticsSummary summary, boolean success) {
        return new StatisticsSummaryResponse(success, summary.getMessage(), summary.getSource(), summary.getSampleCount(),
            summary.getTemperatureAvg(), summary.getTemperatureMax(), summary.getTemperatureMin(), summary.getTemperatureStd(),
            summary.getHumidityAvg(), summary.getHumidityMax(), summary.getHumidityMin(), summary.getHumidityStd(),
            summary.getGasAvg(), summary.getGasMax(), summary.getGasMin(), summary.getGasStd(),
            summary.getTemperatureChangeRate(), summary.getHumidityChangeRate(), summary.getGasChangeRate(),
            summary.getVolatilityLevel(), summary.getTrend());
    }

    public List<SensorData> chronological(String source, int limit) {
        List<SensorData> desc = sensorDataService.recentBySource(source, limit);
        List<SensorData> data = new ArrayList<>(desc);
        Collections.reverse(data);
        return data;
    }

    private StatisticsSummaryResponse insufficient(String source, int sampleCount, String message) {
        return new StatisticsSummaryResponse(false, message, source, sampleCount,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);
    }

    private MetricStats stats(List<Double> values) {
        DoubleSummaryStatistics s = values.stream().mapToDouble(Double::doubleValue).summaryStatistics();
        double avg = s.getAverage();
        double std = 0.0;
        if (values.size() > 1) {
            double variance = values.stream().mapToDouble(v -> Math.pow(v - avg, 2)).sum() / (values.size() - 1);
            std = Math.sqrt(variance);
        }
        return new MetricStats(avg, s.getMax(), s.getMin(), std);
    }

    private String trendByRate(double rate, double threshold) {
        if (Math.abs(rate) < threshold) {
            return "STABLE";
        }
        return rate > 0 ? "UP" : "DOWN";
    }

    private String overallTrend(double tempRate, double humidityRate, double gasRate) {
        String temp = trendByRate(tempRate, 0.02);
        String humidity = trendByRate(humidityRate, 0.05);
        String gas = trendByRate(gasRate, 0.1);
        if ("UP".equals(temp) || "UP".equals(gas) || "UP".equals(humidity)) {
            return "UP";
        }
        if ("DOWN".equals(temp) || "DOWN".equals(gas) || "DOWN".equals(humidity)) {
            return "DOWN";
        }
        return "STABLE";
    }

    private String volatility(double tempStd, double gasStd) {
        String tempLevel = tempStd > 1.5 ? "HIGH" : tempStd >= 0.5 ? "MEDIUM" : "LOW";
        String gasLevel = gasStd > 15 ? "HIGH" : gasStd >= 5 ? "MEDIUM" : "LOW";
        if ("HIGH".equals(tempLevel) || "HIGH".equals(gasLevel)) {
            return "HIGH";
        }
        if ("MEDIUM".equals(tempLevel) || "MEDIUM".equals(gasLevel)) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String trendText(String trend) {
        return switch (trend) {
            case "UP" -> "上升";
            case "DOWN" -> "下降";
            default -> "平稳";
        };
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private record MetricStats(double avg, double max, double min, double std) {
    }
}
