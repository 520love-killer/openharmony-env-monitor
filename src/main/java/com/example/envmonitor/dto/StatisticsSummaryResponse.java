package com.example.envmonitor.dto;

public record StatisticsSummaryResponse(
    boolean success,
    String message,
    String source,
    int sampleCount,
    Double temperatureAvg,
    Double temperatureMax,
    Double temperatureMin,
    Double temperatureStd,
    Double humidityAvg,
    Double humidityMax,
    Double humidityMin,
    Double humidityStd,
    Double gasAvg,
    Double gasMax,
    Double gasMin,
    Double gasStd,
    Double temperatureChangeRate,
    Double humidityChangeRate,
    Double gasChangeRate,
    String volatilityLevel,
    String trend
) {
}
