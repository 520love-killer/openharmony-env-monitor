package com.example.envmonitor.dto;

import java.time.LocalDateTime;

public record AnomalyItem(
    String type,
    LocalDateTime time,
    String level,
    String reason,
    String suggestion,
    Long relatedSensorDataId
) {
}
