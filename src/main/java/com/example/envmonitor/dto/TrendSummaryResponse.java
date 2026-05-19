package com.example.envmonitor.dto;

public record TrendSummaryResponse(
    boolean success,
    String message,
    String source,
    int sampleCount,
    String temperatureTrend,
    String humidityTrend,
    String gasTrend,
    String overallTrend,
    String explanation
) {
}
