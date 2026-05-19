package com.example.envmonitor.controller;

import com.example.envmonitor.dto.AgentChatRequest;
import com.example.envmonitor.dto.AgentChatResponse;
import com.example.envmonitor.service.AgentMemoryService;
import com.example.envmonitor.service.AgentPromptService;
import com.example.envmonitor.service.AgentService;
import com.example.envmonitor.service.LlmService;
import com.example.envmonitor.util.DataSourceUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentService agentService;
    private final AgentMemoryService agentMemoryService;
    private final AgentPromptService agentPromptService;
    private final LlmService llmService;

    public AgentController(
        AgentService agentService,
        AgentMemoryService agentMemoryService,
        AgentPromptService agentPromptService,
        LlmService llmService
    ) {
        this.agentService = agentService;
        this.agentMemoryService = agentMemoryService;
        this.agentPromptService = agentPromptService;
        this.llmService = llmService;
    }

    @PostMapping("/chat")
    public AgentChatResponse chat(@RequestBody AgentChatRequest request) {
        return agentService.chat(request);
    }

    /**
     * Streaming agent endpoint. Uses SSE to stream tokens from DeepSeek or mock.
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody AgentChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout

        CompletableFuture.runAsync(() -> {
            try {
                agentService.streamChat(
                    request,
                    // onStart
                    status -> sendSse(emitter, "status", status),
                    // onChunk
                    chunk -> sendSse(emitter, "chunk", chunk),
                    // onDone
                    done -> {
                        sendSse(emitter, "done", done);
                        emitter.complete();
                    },
                    // onError
                    error -> {
                        sendSse(emitter, "error", error);
                        emitter.complete();
                    }
                );
            } catch (Exception e) {
                sendSse(emitter, "error", e.getMessage());
                emitter.complete();
            }
        });

        emitter.onTimeout(() -> {
            try { sendSse(emitter, "error", "请求超时"); } catch (Exception ex) { /* ignore */ }
            emitter.complete();
        });

        emitter.onError(ex -> {
            try { emitter.complete(); } catch (Exception ex2) { /* ignore */ }
        });

        return emitter;
    }

    private void sendSse(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (Exception e) {
            // client disconnected, ignore
        }
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("mode", llmService.isAvailable() ? "deepseek" : "mock");
        status.put("hasApiKey", llmService.isAvailable());
        status.put("model", llmService.getModel());
        status.put("provider", llmService.getProvider());
        status.put("ragEnabled", true);
        status.put("memoryEnabled", true);
        status.put("streamingEnabled", true);
        status.put("agentName", AgentPromptService.AGENT_NAME);
        return status;
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
