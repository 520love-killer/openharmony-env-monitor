package com.example.envmonitor.dto;

import java.util.List;

public record AgentChatResponse(
    String sessionId,
    String agentName,
    String answer,
    List<String> usedTools,
    String dataSource,
    String confidence,
    String createdAt,
    String mode,
    String model
) {
    // backward compatible constructor for internal use
    public AgentChatResponse(
        String sessionId, String agentName, String answer,
        List<String> usedTools, String dataSource, String confidence, String createdAt
    ) {
        this(sessionId, agentName, answer, usedTools, dataSource, confidence, createdAt, "mock", null);
    }
}
