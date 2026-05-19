package com.example.envmonitor.dto;

import java.util.Map;

public record AgentToolResult(
    String toolName,
    boolean success,
    Object data,
    String summary
) {
    public static AgentToolResult of(String toolName, boolean success, Object data, String summary) {
        return new AgentToolResult(toolName, success, data, summary);
    }
}
