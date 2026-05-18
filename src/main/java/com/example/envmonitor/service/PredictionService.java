package com.example.envmonitor.service;

import com.example.envmonitor.entity.SensorData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PredictionService {
    private final SensorDataService sensorDataService;

    public PredictionService(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    public Map<String, Object> predictNext(String source) {
        String dataSource = sensorDataService.normalizeSource(source);
        List<SensorData> recentDesc = sensorDataService.recentBySource(dataSource, 10);
        List<SensorData> recent = new ArrayList<>(recentDesc);
        Collections.reverse(recent);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dataSource", dataSource);
        result.put("sampleCount", recent.size());
        result.put("method", "average plus simple trend from recent 10 records");

        if (recent.size() < 3) {
            result.put("hasPrediction", false);
            result.put("message", "演示数据不足，暂不进行预测。");
            result.put("status", "NO_DATA");
            return result;
        }

        double temperature = predictMetric(recent.stream().map(SensorData::getTemperature).toList());
        double humidity = predictMetric(recent.stream().map(SensorData::getHumidity).toList());
        double gas = predictMetric(recent.stream().map(SensorData::getGas).toList());

        result.put("hasPrediction", true);
        result.put("temperature", round1(temperature));
        result.put("humidity", round1(humidity));
        result.put("gas", round1(gas));
        result.put("status", sensorDataService.judgeStatus(temperature, humidity, gas));
        return result;
    }

    private double predictMetric(List<Double> values) {
        double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }
        double average = sum / values.size();

        double totalDelta = 0.0;
        for (int i = 1; i < values.size(); i++) {
            totalDelta += values.get(i) - values.get(i - 1);
        }
        double averageDelta = totalDelta / (values.size() - 1);

        return (average * 0.6) + ((values.get(values.size() - 1) + averageDelta) * 0.4);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
