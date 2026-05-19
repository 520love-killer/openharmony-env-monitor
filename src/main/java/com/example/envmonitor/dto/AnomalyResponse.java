package com.example.envmonitor.dto;

import java.util.List;

public record AnomalyResponse(
    boolean success,
    String message,
    String source,
    int sampleCount,
    boolean hasAnomaly,
    int anomalyCount,
    List<AnomalyItem> items
) {
}
