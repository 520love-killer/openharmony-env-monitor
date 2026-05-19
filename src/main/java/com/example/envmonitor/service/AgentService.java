package com.example.envmonitor.service;

import com.example.envmonitor.dto.AgentChatRequest;
import com.example.envmonitor.dto.AgentChatResponse;
import com.example.envmonitor.dto.AgentToolResult;
import com.example.envmonitor.util.DataSourceUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgentService {
    private static final Logger log = LoggerFactory.getLogger(AgentService.class);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AgentToolService agentToolService;
    private final AgentPromptService agentPromptService;
    private final AgentRagService agentRagService;
    private final AgentMemoryService agentMemoryService;
    private final SensorDataService sensorDataService;
    private final LlmService llmService;

    public AgentService(
        AgentToolService agentToolService,
        AgentPromptService agentPromptService,
        AgentRagService agentRagService,
        AgentMemoryService agentMemoryService,
        SensorDataService sensorDataService,
        LlmService llmService
    ) {
        this.agentToolService = agentToolService;
        this.agentPromptService = agentPromptService;
        this.agentRagService = agentRagService;
        this.agentMemoryService = agentMemoryService;
        this.sensorDataService = sensorDataService;
        this.llmService = llmService;
    }

    public AgentChatResponse chat(AgentChatRequest request) {
        String sessionId = agentMemoryService.ensureSession(request.sessionId(), request.message());
        String source = sensorDataService.normalizeSource(
            request.source() != null ? request.source() : DataSourceUtils.REAL_SERIAL
        );

        agentMemoryService.saveMessage(sessionId, "user", request.message(), null, source, null);

        // Build conversation history from request or database
        List<Map<String, String>> history = request.history();
        if (history == null || history.isEmpty()) {
            history = agentMemoryService.getRecentConversation(sessionId, 10);
        }

        List<String> toolNames = agentToolService.selectTools(request.message());
        List<AgentToolResult> toolResults = agentToolService.executeTools(toolNames, source);

        boolean isReal = DataSourceUtils.isReal(source);
        boolean isMock = DataSourceUtils.MOCK.equals(source);
        String confidence = calculateConfidence(toolResults, isReal, isMock);
        boolean hasRealData = checkRealDataAvailability(source);

        String structuredAnswer = buildAnswer(request.message(), toolResults, source, isReal, hasRealData, isMock);

        // Try DeepSeek LLM first; fall back to structured mock answer on failure or no key
        LlmService.LlmChatResponse llmResponse = tryLlmChat(request.message(), toolResults,
            source, isReal, hasRealData, isMock, history);

        String answer;
        String mode;
        String model;
        if (llmResponse != null && llmResponse.success()) {
            answer = llmResponse.content();
            mode = "deepseek";
            model = llmResponse.model();
        } else {
            answer = structuredAnswer;
            mode = "mock";
            model = null;
        }

        agentMemoryService.saveMessage(sessionId, "agent", answer, toolNames, source, confidence);

        return new AgentChatResponse(
            sessionId,
            AgentPromptService.AGENT_NAME,
            answer,
            toolNames,
            source,
            confidence,
            LocalDateTime.now().format(DT_FMT),
            mode,
            model
        );
    }

    /**
     * Stream agent response. Calls onStart with metadata, onChunk with text deltas,
     * onDone with final metadata (toolNames, source, confidence).
     */
    public void streamChat(AgentChatRequest request,
                           Consumer<String> onStart,
                           Consumer<String> onChunk,
                           Consumer<StreamDone> onDone,
                           Consumer<String> onError) {
        String sessionId = agentMemoryService.ensureSession(request.sessionId(), request.message());
        String source = sensorDataService.normalizeSource(
            request.source() != null ? request.source() : DataSourceUtils.REAL_SERIAL
        );

        agentMemoryService.saveMessage(sessionId, "user", request.message(), null, source, null);

        List<Map<String, String>> history = request.history();
        if (history == null || history.isEmpty()) {
            history = agentMemoryService.getRecentConversation(sessionId, 10);
        }

        List<String> toolNames = agentToolService.selectTools(request.message());
        List<AgentToolResult> toolResults = agentToolService.executeTools(toolNames, source);

        boolean isReal = DataSourceUtils.isReal(source);
        boolean isMock = DataSourceUtils.MOCK.equals(source);
        String confidence = calculateConfidence(toolResults, isReal, isMock);
        boolean hasRealData = checkRealDataAvailability(source);

        // Send initial metadata
        try {
            onStart.accept("开始分析...");
        } catch (Exception e) { /* ignore */ }

        // Build context for LLM
        String structuredAnswer = buildAnswer(request.message(), toolResults, source, isReal, hasRealData, isMock);
        List<Map<String, String>> messages = buildLlmMessages(request.message(), toolResults,
            source, isReal, hasRealData, isMock, history);

        // Try DeepSeek streaming first, fallback to mock streaming
        boolean llmAvailable = llmService.isAvailable();
        String modelName = llmAvailable ? llmService.getModel() : null;

        if (llmAvailable) {
            boolean streamOk = llmService.streamChat(messages,
                chunk -> {
                    try { onChunk.accept(chunk); } catch (Exception e) { /* ignore */ }
                },
                error -> {
                    // DeepSeek failed, fallback to mock streaming
                    log.warn("DeepSeek stream failed, falling back to mock: {}", error);
                    mockStreamAnswer(structuredAnswer, onChunk);
                }
            );
            if (streamOk) {
                StreamDone done = new StreamDone(sessionId, AgentPromptService.AGENT_NAME,
                    toolNames.stream().toList(), source, confidence,
                    LocalDateTime.now().format(DT_FMT), "deepseek", modelName);
                try { onDone.accept(done); } catch (Exception e) { /* ignore */ }
                return;
            }
        }

        // Mock streaming fallback
        mockStreamAnswer(structuredAnswer, onChunk);
        StreamDone done = new StreamDone(sessionId, AgentPromptService.AGENT_NAME,
            toolNames.stream().toList(), source, confidence,
            LocalDateTime.now().format(DT_FMT), "mock", null);
        try { onDone.accept(done); } catch (Exception e) { /* ignore */ }
    }

    /**
     * Simulate streaming by breaking the answer into small chunks with delays.
     * Called on a separate thread so the SSE connection stays open.
     */
    private void mockStreamAnswer(String answer, Consumer<String> onChunk) {
        if (answer == null || answer.isEmpty()) {
            onChunk.accept("当前没有足够数据来回答你的问题。");
            return;
        }

        // Break into character chunks for realistic streaming feel
        int chunkSize = 3;
        for (int i = 0; i < answer.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, answer.length());
            String chunk = answer.substring(i, end);
            onChunk.accept(chunk);
            try {
                Thread.sleep(30); // simulate typing delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private List<Map<String, String>> buildLlmMessages(String message, List<AgentToolResult> results,
                                                        String source, boolean isReal, boolean hasEnough, boolean isMock,
                                                        List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
            "role", "system",
            "content", agentPromptService.buildSystemPrompt(source, isReal, hasEnough, isMock)
        ));

        // Add conversation history (last 10 messages, each truncated to 1000 chars)
        if (history != null) {
            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                Map<String, String> msg = history.get(i);
                String content = msg.get("content");
                // Truncate long messages to 1000 chars
                if (content != null && content.length() > 1000) {
                    content = content.substring(0, 1000) + "...";
                }
                messages.add(Map.of("role", msg.get("role"), "content", content));
            }
        }

        messages.add(Map.of(
            "role", "user",
            "content", "用户问题：" + message + "\n\n工具调用结果：\n" + summarizeToolResults(results)
                + "\n\n请基于以上真实工具结果回答，不要编造不存在的数据。"
        ));
        return messages;
    }

    private LlmService.LlmChatResponse tryLlmChat(String message, List<AgentToolResult> results,
                                                   String source, boolean isReal, boolean hasEnoughData, boolean isMock,
                                                   List<Map<String, String>> history) {
        if (!llmService.isAvailable()) {
            return new LlmService.LlmChatResponse(false, "API Key 未配置，已降级为本地结构化回复。", null);
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
            "role", "system",
            "content", agentPromptService.buildSystemPrompt(source, isReal, hasEnoughData, false)
        ));

        // Add conversation history
        if (history != null) {
            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                Map<String, String> msg = history.get(i);
                String content = msg.get("content");
                if (content != null && content.length() > 1000) {
                    content = content.substring(0, 1000) + "...";
                }
                messages.add(Map.of("role", msg.get("role"), "content", content));
            }
        }

        messages.add(Map.of(
            "role", "user",
            "content", "用户问题：" + message + "\n\n工具调用结果：\n" + summarizeToolResults(results)
                + "\n\n请基于以上真实工具结果回答，不要编造不存在的数据。"
        ));

        return llmService.chat(messages);
    }

    private String summarizeToolResults(List<AgentToolResult> results) {
        StringBuilder sb = new StringBuilder();
        for (AgentToolResult result : results) {
            sb.append("- ").append(result.toolName())
                .append(": success=").append(result.success())
                .append(", summary=").append(result.summary()).append("\n");
        }
        return sb.toString();
    }

    private String buildAnswer(String message, List<AgentToolResult> results, String source,
                                boolean isReal, boolean hasEnoughData, boolean isMock) {
        StringBuilder sb = new StringBuilder();

        if (isMock) {
            sb.append("**注意：当前为模拟数据，不代表真实硬件采集结果。**\n\n");
        } else if (!isReal) {
            sb.append("**当前没有真实硬件数据。**\n\n");
        } else if (!hasEnoughData) {
            sb.append("**真实数据不足 5 条，无法提供可靠分析。**\n\n");
        }

        if (message.contains("算法") || message.contains("原理") || message.contains("精准度")
            || message.contains("怎么预测") || message.contains("什么方法")) {
            sb.append(agentPromptService.buildAlgorithmExplanation());
            sb.append("\n\n---\n\n");
        }

        if (message.contains("报告") || message.contains("摘要") || message.contains("总结")) {
            sb.append(generateReportSummary(results, source));
            return sb.toString();
        }

        for (AgentToolResult result : results) {
            if (!result.success()) continue;
            sb.append(formatToolResult(result, message));
            sb.append("\n\n");
        }

        if (sb.isEmpty()) {
            sb.append("无法获取足够数据来回答你的问题。");
            if (!hasEnoughData) {
                sb.append("请确保 Hi3861 设备已连接并正在发送数据。");
            }
        }

        sb.append("---\n");
        sb.append("数据来源：").append(source).append(" | ");
        sb.append("数据条数：").append(countDataPoints(results));
        return sb.toString();
    }

    private String formatToolResult(AgentToolResult result, String question) {
        return switch (result.toolName()) {
            case "getRecent50Data" -> {
                StringBuilder sb = new StringBuilder();
                sb.append("### 最近温度数据\n\n");
                sb.append(result.summary()).append("\n\n");
                if (result.data() instanceof List<?> list && !list.isEmpty()) {
                    sb.append("| # | 温度 | 湿度 | 燃气 |\n");
                    sb.append("|---|------|------|------|\n");
                    int count = 0;
                    for (Object item : list) {
                        if (count++ >= 10) break;
                        if (item instanceof Map<?, ?> m) {
                            sb.append(String.format("| %d | %s℃ | %s%% | %sppm |\n",
                                count,
                                formatVal(m.get("temperature")),
                                formatVal(m.get("humidity")),
                                formatVal(m.get("gas"))));
                        }
                    }
                    if (list.size() > 10) sb.append("| ... | ... | ... | ... |\n");
                }
                yield sb.toString();
            }
            case "getLatestSensorData" -> result.summary();
            case "getAnalyticsSummary" -> {
                if (result.data() instanceof com.example.envmonitor.dto.StatisticsSummaryResponse s && s.success()) {
                    yield String.format("### 统计分析\n\n- 平均温度：%.2f℃\n- 最高温度：%.2f℃\n- 最低温度：%.2f℃\n- 标准差：%.2f\n- 波动程度：%s\n- 温度变化率：%.3f℃/min\n- 整体趋势：%s\n- 样本数：%d",
                        s.temperatureAvg(), s.temperatureMax(), s.temperatureMin(),
                        s.temperatureStd(), s.volatilityLevel(),
                        s.temperatureChangeRate(), s.trend(), s.sampleCount());
                }
                yield result.summary();
            }
            case "getTemperatureForecast" -> {
                if (result.data() instanceof com.example.envmonitor.dto.ForecastResponse f && f.success()) {
                    yield String.format("### 温度预测\n\n- 当前温度：%.2f℃\n- 未来 5 分钟预测：%.2f℃\n- 未来 10 分钟预测：%.2f℃\n- 滑动平均 5/10min：%.2f / %.2f℃\n- 线性回归 5/10min：%.2f / %.2f℃\n- 指数平滑 5/10min：%.2f / %.2f℃\n- 趋势：%s\n- 置信度：%s",
                        f.currentTemperature(), f.finalForecast5min(), f.finalForecast10min(),
                        f.movingAverageForecast5min(), f.movingAverageForecast10min(),
                        f.linearRegressionForecast5min(), f.linearRegressionForecast10min(),
                        f.exponentialSmoothingForecast5min(), f.exponentialSmoothingForecast10min(),
                        f.trend(), f.confidence());
                }
                yield result.summary();
            }
            case "getAnomalyDetection" -> {
                if (result.data() instanceof com.example.envmonitor.dto.AnomalyResponse a && a.success()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("### 异常检测\n\n");
                    sb.append(String.format("异常数量：%d 个\n\n", a.anomalyCount()));
                    if (a.hasAnomaly() && a.items() != null) {
                        for (var item : a.items()) {
                            sb.append(String.format("- **%s** [%s] %s → %s\n",
                                item.type(), item.level(), item.reason(), item.suggestion()));
                        }
                    } else {
                        sb.append("当前未发现明显异常。\n");
                    }
                    yield sb.toString();
                }
                yield result.summary();
            }
            case "getDatabaseStatus" -> result.summary();
            case "getScenarioAnalysis" -> {
                if (result.data() instanceof com.example.envmonitor.dto.ScenarioAnalysisResponse s) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("### 场景化环境分析\n\n");
                    sb.append(String.format("- 场景：%s\n", s.scenarioName()));
                    sb.append(String.format("- 综合评分：%d 分（%s）\n", s.score(), s.levelName()));
                    sb.append(String.format("- 置信度：%s\n", s.confidence()));
                    sb.append(String.format("- 样本数：%d\n\n", s.sampleCount()));
                    if (!s.risks().isEmpty()) {
                        sb.append("**风险提示：**\n");
                        for (var r : s.risks()) {
                            sb.append(String.format("- [%s] %s\n", r.level(), r.message()));
                        }
                        sb.append("\n");
                    }
                    if (!s.advices().isEmpty()) {
                        sb.append("**建议：**\n");
                        for (var a : s.advices()) {
                            sb.append(String.format("- %s：%s\n", a.title(), a.content()));
                        }
                    }
                    yield sb.toString();
                }
                yield result.summary();
            }
            default -> result.summary();
        };
    }

    private String generateReportSummary(List<AgentToolResult> results, String source) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 实验报告摘要\n\n");
        sb.append("### 实验目的\n");
        sb.append("基于 OpenHarmony Hi3861 环境监测系统，分析温度、湿度、燃气数据变化规律，验证传感器数据采集、无线传输和云端分析能力。\n\n");
        sb.append("### 实验环境\n");
        sb.append("- 硬件：Hi3861 + MQ-2 + AHT20 + OLED\n");
        sb.append("- 后端：Spring Boot + MySQL + Caffeine Cache\n");
        sb.append("- 数据源：").append(source).append("\n\n");

        sb.append("### 数据分析\n");
        for (AgentToolResult result : results) {
            if (result.success() && result.summary() != null) {
                sb.append("- ").append(result.summary()).append("\n");
            }
        }
        sb.append("\n### 结果总结\n");
        sb.append("系统成功采集并分析了环境数据，能够实时监测温度变化趋势，检测异常风险。\n");
        sb.append("当前使用轻量级预测算法（滑动平均 + 线性回归 + 指数平滑），适合短期趋势估计。\n");
        sb.append("\n### 后续改进\n");
        sb.append("- 可接入更精确的时序预测模型\n");
        sb.append("- 可扩展为多设备组网监测\n");
        sb.append("- 可增加更多传感器类型（PM2.5、CO2等）\n");
        return sb.toString();
    }

    private String calculateConfidence(List<AgentToolResult> results, boolean isReal, boolean isMock) {
        long successCount = results.stream().filter(AgentToolResult::success).count();
        if (successCount == 0) return "LOW";
        if (isMock) return "LOW";
        if (!isReal) return "LOW";
        if (successCount >= 3) return "MEDIUM";
        return "LOW";
    }

    private boolean checkRealDataAvailability(String source) {
        if (!DataSourceUtils.isReal(source)) return false;
        List<com.example.envmonitor.entity.SensorData> recent = sensorDataService.recentBySource(source, 50);
        return recent.size() >= 5;
    }

    private int countDataPoints(List<AgentToolResult> results) {
        for (AgentToolResult r : results) {
            if ("getRecent50Data".equals(r.toolName()) && r.data() instanceof List<?> list) {
                return list.size();
            }
            if (r.data() instanceof com.example.envmonitor.dto.StatisticsSummaryResponse summary) {
                return summary.sampleCount();
            }
            if (r.data() instanceof com.example.envmonitor.dto.ForecastResponse forecast) {
                return forecast.sampleCount();
            }
            if (r.data() instanceof com.example.envmonitor.dto.AnomalyResponse anomaly) {
                return anomaly.sampleCount();
            }
        }
        return 0;
    }

    private String formatVal(Object val) {
        if (val instanceof Number n) return String.format("%.1f", n.doubleValue());
        return val != null ? val.toString() : "-";
    }

    public Map<String, Object> getContext(String source) {
        String normalized = sensorDataService.normalizeSource(source);
        boolean isReal = DataSourceUtils.isReal(normalized);
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("agentName", AgentPromptService.AGENT_NAME);
        ctx.put("source", normalized);
        ctx.put("isRealData", isReal);

        var latest = sensorDataService.latestBySource(normalized);
        ctx.put("hasLatestData", latest.isPresent());
        ctx.put("latestData", latest.orElse(null));

        var stats = agentToolService.executeTools(List.of("getAnalyticsSummary"), normalized);
        ctx.put("hasStatistics", !stats.isEmpty() && stats.getFirst().success());

        var forecast = agentToolService.executeTools(List.of("getTemperatureForecast"), normalized);
        ctx.put("hasForecast", !forecast.isEmpty() && forecast.getFirst().success());

        var anomaly = agentToolService.executeTools(List.of("getAnomalyDetection"), normalized);
        ctx.put("hasAnomalies", !anomaly.isEmpty() && anomaly.getFirst().success());

        ctx.put("totalSampleCount", sensorDataService.recentBySource(normalized, 1000).size());
        ctx.put("hasEnoughRealData", isReal && sensorDataService.recentBySource(normalized, 5).size() >= 5);

        return ctx;
    }

    public record StreamDone(String sessionId, String agentName, List<String> usedTools,
                             String dataSource, String confidence, String createdAt,
                             String mode, String model) {
    }
}
