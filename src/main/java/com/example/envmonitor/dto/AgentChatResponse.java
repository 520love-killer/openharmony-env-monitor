package com.example.envmonitor.dto;

import java.util.List;

public record AgentChatResponse(
    String sessionId,
    String agentName,
    String answer,
    List<String> usedTools,
    String dataSource,
    String confidence,
    String createdAt
) {
}
