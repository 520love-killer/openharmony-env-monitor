package com.example.envmonitor.dto;

import java.util.Map;

public record AgentContextResponse(
    String source,
    String agentName,
    boolean hasEnoughRealData,
    long totalSampleCount,
    boolean hasLatestData,
    boolean hasStatistics,
    boolean hasForecast,
    boolean hasAnomalies,
    Map<String, Object> details
) {
}
