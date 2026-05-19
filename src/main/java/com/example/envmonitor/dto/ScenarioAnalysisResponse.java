package com.example.envmonitor.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ScenarioAnalysisResponse(
    String scenario,
    String scenarioName,
    String userRole,
    String userRoleName,
    int score,
    String level,
    String levelName,
    String confidence,
    String summary,
    List<ScenarioRiskItem> risks,
    List<ScenarioAdviceItem> advices,
    ScenarioMetricStatus metrics,
    ScenarioScoreBreakdown scoreBreakdown,
    List<String> algorithmNotes,
    String dataSource,
    int sampleCount,
    String updatedAt
) {
    public record ScenarioRiskItem(
        String type,
        String level,
        String message
    ) {
    }

    public record ScenarioAdviceItem(
        String type,
        String title,
        String content
    ) {
    }

    public record ScenarioMetricStatus(
        String temperatureStatus,
        String humidityStatus,
        String gasStatus,
        String trendStatus
    ) {
    }

    public record ScenarioScoreBreakdown(
        int temperatureScore,
        int humidityScore,
        int gasScore,
        int trendScore,
        int anomalyScore
    ) {
    }
}
