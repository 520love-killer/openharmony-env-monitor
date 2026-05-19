package com.example.envmonitor.dto;

import java.util.List;

public record AgentAnalyzeResponse(
    boolean success,
    String answer,
    List<String> usedTools
) {
}
