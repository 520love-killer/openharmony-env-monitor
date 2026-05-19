package com.example.envmonitor.dto;

public record ForecastResponse(
    boolean success,
    String message,
    String source,
    int sampleCount,
    Double currentTemperature,
    Double movingAverageForecast5min,
    Double movingAverageForecast10min,
    Double linearRegressionForecast5min,
    Double linearRegressionForecast10min,
    Double exponentialSmoothingForecast5min,
    Double exponentialSmoothingForecast10min,
    Double finalForecast5min,
    Double finalForecast10min,
    String trend,
    String confidence
) {
}
