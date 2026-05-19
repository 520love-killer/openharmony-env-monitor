package com.example.envmonitor.dto;

import java.util.List;
import java.util.Map;

public record AgentChatRequest(
    String sessionId,
    String role,
    String message,
    String source,
    List<Map<String, String>> history
) {
    // backward compatible constructor for old clients
    public AgentChatRequest(String sessionId, String message, String source) {
        this(sessionId, null, message, source, null);
    }
}
