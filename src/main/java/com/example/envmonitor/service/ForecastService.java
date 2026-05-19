package com.example.envmonitor.service;

import com.example.envmonitor.dto.ForecastResponse;
import com.example.envmonitor.entity.SensorData;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForecastService {
    private final AnalyticsService analyticsService;
    private final SensorDataService sensorDataService;

    public ForecastService(AnalyticsService analyticsService, SensorDataService sensorDataService) {
        this.analyticsService = analyticsService;
        this.sensorDataService = sensorDataService;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "forecastCache", key = "#source + '_' + #limit")
    public ForecastResponse temperatureForecast(String source, int limit) {
        String dataSource = sensorDataService.normalizeSource(source);
        List<SensorData> data = analyticsService.chronological(dataSource, limit);
        if (data.size() < 5) {
            return new ForecastResponse(false, "真实数据不足，至少需要 5 条温度数据才能预测。", dataSource, data.size(),
                null, null, null, null, null, null, null, null, null, null, null);
        }

        double current = data.get(data.size() - 1).getTemperature();
        double changeRate = changeRatePerStep(data);
        double recentAverage = data.subList(Math.max(0, data.size() - 5), data.size())
            .stream().mapToDouble(SensorData::getTemperature).average().orElse(current);
        double moving5 = recentAverage + changeRate * 5;
        double moving10 = recentAverage + changeRate * 10;

        double[] linear = linearRegression(data);
        double regression5 = linear[1] + linear[0] * (data.size() + 4);
        double regression10 = linear[1] + linear[0] * (data.size() + 9);

        double ewma = exponentialSmoothing(data, 0.3);
        double ewma5 = ewma + changeRate * 5;
        double ewma10 = ewma + changeRate * 10;

        double final5 = average(moving5, regression5, ewma5);
        double final10 = average(moving10, regression10, ewma10);
        String trend = final10 - current > 0.5 ? "UP" : current - final10 > 0.5 ? "DOWN" : "STABLE";
        double tempStd = temperatureStd(data);
        String confidence = data.size() >= 50 && tempStd <= 1.5 ? "HIGH" : data.size() >= 20 ? "MEDIUM" : "LOW";
        String message = switch (trend) {
            case "UP" -> "温度呈缓慢上升趋势，建议继续观察";
            case "DOWN" -> "温度呈下降趋势，建议关注环境变化";
            default -> "温度整体平稳";
        };

        return new ForecastResponse(true, message, dataSource, data.size(), round2(current),
            round2(moving5), round2(moving10), round2(regression5), round2(regression10),
            round2(ewma5), round2(ewma10), round2(final5), round2(final10), trend, confidence);
    }

    private double changeRatePerStep(List<SensorData> data) {
        if (data.size() < 2) {
            return 0.0;
        }
        return (data.get(data.size() - 1).getTemperature() - data.get(0).getTemperature()) / Math.max(1, data.size() - 1);
    }

    private double[] linearRegression(List<SensorData> data) {
        int n = data.size();
        double avgX = (n - 1) / 2.0;
        double avgY = data.stream().mapToDouble(SensorData::getTemperature).average().orElse(0.0);
        double numerator = 0.0;
        double denominator = 0.0;
        for (int i = 0; i < n; i++) {
            double dx = i - avgX;
            numerator += dx * (data.get(i).getTemperature() - avgY);
            denominator += dx * dx;
        }
        double slope = denominator == 0.0 ? 0.0 : numerator / denominator;
        double intercept = avgY - slope * avgX;
        return new double[] {slope, intercept};
    }

    private double exponentialSmoothing(List<SensorData> data, double alpha) {
        double value = data.get(0).getTemperature();
        for (int i = 1; i < data.size(); i++) {
            value = alpha * data.get(i).getTemperature() + (1 - alpha) * value;
        }
        return value;
    }

    private double temperatureStd(List<SensorData> data) {
        double avg = data.stream().mapToDouble(SensorData::getTemperature).average().orElse(0.0);
        if (data.size() < 2) {
            return 0.0;
        }
        return Math.sqrt(data.stream().mapToDouble(v -> Math.pow(v.getTemperature() - avg, 2)).sum() / (data.size() - 1));
    }

    private double average(double... values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
