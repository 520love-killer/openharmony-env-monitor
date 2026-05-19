package com.example.envmonitor.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PredictionService {
    private final ForecastService forecastService;

    public PredictionService(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    public Map<String, Object> predictNext(String source) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("temperatureForecast", forecastService.temperatureForecast(source, 50));
        result.put("message", "兼容旧接口：请优先使用 /api/forecast/temperature");
        return result;
    }
}
