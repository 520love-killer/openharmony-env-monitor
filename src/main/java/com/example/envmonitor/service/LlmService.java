package com.example.envmonitor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LlmService {
    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private static final String DEFAULT_DEEPSEEK_BASE = "https://api.deepseek.com";
    private static final String DEFAULT_KIMI_BASE = "https://api.moonshot.cn";
    private static final String DEFAULT_DEEPSEEK_MODEL = "deepseek-chat";
    private static final String DEFAULT_KIMI_MODEL = "moonshot-v1-8k";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String provider;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public LlmService(
        ObjectMapper objectMapper,
        @Value("${agent.provider:mock}") String provider,
        @Value("${agent.api-key:}") String apiKey,
        @Value("${agent.base-url:}") String baseUrl,
        @Value("${agent.model:}") String model
    ) {
        this.objectMapper = objectMapper;
        this.provider = firstNonBlank(provider, "mock").toLowerCase();
        boolean useKimiDefaults = isKimiProvider(this.provider) || hasAnyEnv("KIMI_API_KEY", "MOONSHOT_API_KEY");
        this.apiKey = firstNonBlank(
            apiKey,
            System.getenv("AGENT_API_KEY"),
            System.getenv("KIMI_API_KEY"),
            System.getenv("MOONSHOT_API_KEY"),
            System.getenv("DEEPSEEK_API_KEY")
        );
        this.baseUrl = firstNonBlank(
            baseUrl,
            System.getenv("AGENT_BASE_URL"),
            System.getenv("KIMI_BASE_URL"),
            System.getenv("MOONSHOT_BASE_URL"),
            System.getenv("DEEPSEEK_BASE_URL"),
            useKimiDefaults ? DEFAULT_KIMI_BASE : DEFAULT_DEEPSEEK_BASE
        );
        this.model = firstNonBlank(
            model,
            System.getenv("AGENT_MODEL"),
            System.getenv("KIMI_MODEL"),
            System.getenv("MOONSHOT_MODEL"),
            System.getenv("DEEPSEEK_MODEL"),
            useKimiDefaults ? DEFAULT_KIMI_MODEL : DEFAULT_DEEPSEEK_MODEL
        );
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getProvider() {
        return provider;
    }

    public LlmChatResponse chat(List<Map<String, String>> messages) {
        if (!isAvailable()) {
            return new LlmChatResponse(false, "API Key 未配置，无法调用 LLM。", null);
        }

        try {
            List<Map<String, Object>> apiMessages = new ArrayList<>();
            for (var msg : messages) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("role", msg.get("role"));
                m.put("content", msg.get("content"));
                apiMessages.add(m);
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", apiMessages);
            body.put("temperature", 0.7);
            body.put("max_tokens", 2000);
            body.put("stream", false);

            String json = objectMapper.writeValueAsString(body);
            String url = baseUrl.replaceAll("/$", "") + "/v1/chat/completions";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    return new LlmChatResponse(true, content, model);
                }
                return new LlmChatResponse(false, "LLM 返回空结果。", model);
            } else {
                log.warn("LLM API error: HTTP {}", response.statusCode());
                return new LlmChatResponse(false,
                    String.format("LLM API 调用失败 (HTTP %d)，已降级为 Mock Agent 回答。", response.statusCode()), model);
            }
        } catch (Exception e) {
            log.error("LLM API call failed", e);
            return new LlmChatResponse(false,
                "LLM API 调用异常：" + e.getMessage() + "，已降级为 Mock Agent 回答。", model);
        }
    }

    private boolean isKimiProvider(String provider) {
        return "kimi".equals(provider) || "moonshot".equals(provider);
    }

    private boolean hasAnyEnv(String... names) {
        for (String name : names) {
            String value = System.getenv(name);
            if (value != null && !value.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public record LlmChatResponse(boolean success, String content, String model) {
    }
}
