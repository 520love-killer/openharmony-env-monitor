package com.example.envmonitor.controller;

import com.example.envmonitor.dto.AgentChatRequest;
import com.example.envmonitor.dto.AgentChatResponse;
import com.example.envmonitor.service.AgentMemoryService;
import com.example.envmonitor.service.AgentPromptService;
import com.example.envmonitor.service.AgentService;
import com.example.envmonitor.util.DataSourceUtils;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentService agentService;
    private final AgentMemoryService agentMemoryService;
    private final AgentPromptService agentPromptService;

    public AgentController(
        AgentService agentService,
        AgentMemoryService agentMemoryService,
        AgentPromptService agentPromptService
    ) {
        this.agentService = agentService;
        this.agentMemoryService = agentMemoryService;
        this.agentPromptService = agentPromptService;
    }

    @PostMapping("/chat")
    public AgentChatResponse chat(@RequestBody AgentChatRequest request) {
        return agentService.chat(request);
    }

    @GetMapping("/context")
    public Map<String, Object> context(@RequestParam(defaultValue = DataSourceUtils.REAL_SERIAL) String source) {
        return agentService.getContext(source);
    }

    @GetMapping("/sessions")
    public List<Map<String, Object>> sessions() {
        return agentMemoryService.getSessions();
    }

    @GetMapping("/sessions/{sessionId}")
    public List<Map<String, Object>> sessionMessages(@PathVariable String sessionId) {
        return agentMemoryService.getSessionMessages(sessionId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Map<String, String> deleteSession(@PathVariable String sessionId) {
        agentMemoryService.deleteSession(sessionId);
        return Map.of("status", "deleted", "sessionId", sessionId);
    }

    @PostMapping("/report-summary")
    public AgentChatResponse reportSummary(@RequestBody(required = false) AgentChatRequest request) {
        String source = request != null && request.source() != null ? request.source() : DataSourceUtils.REAL_SERIAL;
        return agentService.chat(new AgentChatRequest(
            request != null ? request.sessionId() : null,
            "请生成实验报告摘要",
            source
        ));
    }

    @PostMapping("/explain-algorithm")
    public Map<String, String> explainAlgorithm() {
        return Map.of(
            "agentName", AgentPromptService.AGENT_NAME,
            "answer", agentPromptService.buildAlgorithmExplanation()
        );
    }
}
