package com.example.envmonitor.controller;

import com.example.envmonitor.dto.AgentAnalyzeRequest;
import com.example.envmonitor.dto.AgentAnalyzeResponse;
import com.example.envmonitor.service.AgentContextService;
import com.example.envmonitor.service.MockAgentService;
import com.example.envmonitor.util.DataSourceUtils;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentContextService agentContextService;
    private final MockAgentService mockAgentService;

    public AgentController(AgentContextService agentContextService, MockAgentService mockAgentService) {
        this.agentContextService = agentContextService;
        this.mockAgentService = mockAgentService;
    }

    @GetMapping("/context")
    public Map<String, Object> context(@RequestParam(defaultValue = DataSourceUtils.REAL_SERIAL) String source) {
        return agentContextService.context(source);
    }

    @PostMapping("/analyze")
    public AgentAnalyzeResponse analyze(@RequestBody(required = false) AgentAnalyzeRequest request) {
        return mockAgentService.analyze(request);
    }
}
