package com.example.envmonitor.service;

import com.example.envmonitor.dto.AgentToolResult;
import com.example.envmonitor.dto.AnomalyResponse;
import com.example.envmonitor.dto.ForecastResponse;
import com.example.envmonitor.dto.StatisticsSummaryResponse;
import com.example.envmonitor.entity.SensorData;
import com.example.envmonitor.util.DataSourceUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AgentToolService {
    private final SensorDataService sensorDataService;
    private final AnalyticsService analyticsService;
    private final ForecastService forecastService;
    private final AnomalyService anomalyService;
    private final SystemStatusService systemStatusService;

    public AgentToolService(
        SensorDataService sensorDataService,
        AnalyticsService analyticsService,
        ForecastService forecastService,
        AnomalyService anomalyService,
        SystemStatusService systemStatusService
    ) {
        this.sensorDataService = sensorDataService;
        this.analyticsService = analyticsService;
        this.forecastService = forecastService;
        this.anomalyService = anomalyService;
        this.systemStatusService = systemStatusService;
    }

    public List<String> selectTools(String message) {
        String msg = message.toLowerCase();
        List<String> tools = new ArrayList<>();

        if (containsAny(msg, "最近温度", "最近数据", "温度列表", "最近50", "recent", "查看温度", "温度数据", "当前温度多少")) {
            tools.add("getRecent50Data");
        }
        if (containsAny(msg, "最新", "latest", "当前环境", "现在温度", "现在湿度", "实时数据", "最新数据")) {
            tools.add("getLatestSensorData");
        }
        if (containsAny(msg, "分析", "趋势", "统计", "平均", "最大", "最小", "标准差", "变化率", "波动", "温度变化", "整体趋势")) {
            tools.add("getAnalyticsSummary");
        }
        if (containsAny(msg, "预测", "forecast", "未来", "5分钟", "10分钟", "趋势估计", "温度会", "下一步", "温度趋势", "当前温度趋势")) {
            tools.add("getTemperatureForecast");
        }
        if (containsAny(msg, "异常", "报警", "风险", "anomaly", "为什么", "原因", "不安全", "警告")) {
            tools.add("getAnomalyDetection");
        }
        if (containsAny(msg, "系统", "状态", "数据库", "缓存", "连接", "db", "cache", "串口")) {
            tools.add("getDatabaseStatus");
        }
        if (containsAny(msg, "实验报告", "报告", "report", "摘要", "总结")) {
            tools.add("getLatestSensorData");
            tools.add("getAnalyticsSummary");
            tools.add("getTemperatureForecast");
            tools.add("getAnomalyDetection");
            tools.add("getDatabaseStatus");
        }
        if (containsAny(msg, "算法", "原理", "怎么预测", "精准度", "置信度", "什么方法", "explain")) {
            tools.add("getAnalyticsSummary");
            tools.add("getTemperatureForecast");
        }

        if (tools.isEmpty()) {
            tools.add("getLatestSensorData");
            tools.add("getAnalyticsSummary");
        }
        return tools.stream().distinct().toList();
    }

    public List<AgentToolResult> executeTools(List<String> toolNames, String source) {
        String normalized = sensorDataService.normalizeSource(source);
        List<AgentToolResult> results = new ArrayList<>();
        for (String tool : toolNames) {
            results.add(execute(tool, normalized));
        }
        return results;
    }

    private AgentToolResult execute(String toolName, String source) {
        return switch (toolName) {
            case "getLatestSensorData" -> {
                var data = sensorDataService.latestBySource(source);
                yield AgentToolResult.of(toolName, data.isPresent(), data.orElse(null),
                    data.map(d -> String.format("最新数据：温度 %.1f℃，湿度 %.1f%%，燃气 %.1fppm，状态 %s，来源 %s",
                        d.getTemperature(), d.getHumidity(), d.getGas(), d.getStatus(), d.getDataSource()))
                        .orElse("当前无数据"));
            }
            case "getRecent50Data" -> {
                List<SensorData> recent = sensorDataService.recentBySource(source, 50);
                List<Map<String, Object>> temps = recent.stream().map(d -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("temperature", d.getTemperature());
                    m.put("humidity", d.getHumidity());
                    m.put("gas", d.getGas());
                    m.put("time", d.getCreatedAt() != null ? d.getCreatedAt().toString() : null);
                    return m;
                }).toList();
                yield AgentToolResult.of(toolName, !recent.isEmpty(), temps,
                    String.format("最近 %d 条数据，温度范围 %.1f~%.1f℃",
                        recent.size(),
                        recent.stream().mapToDouble(SensorData::getTemperature).min().orElse(0),
                        recent.stream().mapToDouble(SensorData::getTemperature).max().orElse(0)));
            }
            case "getAnalyticsSummary" -> {
                StatisticsSummaryResponse summary = analyticsService.summary(source, 50);
                yield AgentToolResult.of(toolName, summary.success(), summary,
                    summary.success()
                        ? String.format("平均温度 %.2f℃，最高 %.2f℃，最低 %.2f℃，标准差 %.2f，波动程度 %s",
                            summary.temperatureAvg(), summary.temperatureMax(), summary.temperatureMin(),
                            summary.temperatureStd(), summary.volatilityLevel())
                        : "数据不足，无法生成统计摘要");
            }
            case "getTemperatureForecast" -> {
                ForecastResponse forecast = forecastService.temperatureForecast(source, 50);
                yield AgentToolResult.of(toolName, forecast.success(), forecast,
                    forecast.success()
                        ? String.format("5分钟预测 %.2f℃，10分钟预测 %.2f℃，趋势 %s，置信度 %s",
                            forecast.finalForecast5min(), forecast.finalForecast10min(),
                            forecast.trend(), forecast.confidence())
                        : "数据不足，无法生成预测");
            }
            case "getAnomalyDetection" -> {
                AnomalyResponse anomaly = anomalyService.detect(source, 50);
                yield AgentToolResult.of(toolName, anomaly.success(), anomaly,
                    anomaly.hasAnomaly()
                        ? String.format("发现 %d 个异常", anomaly.anomalyCount())
                        : "未发现异常");
            }
            case "getDatabaseStatus" -> {
                var status = systemStatusService.databaseStatus();
                yield AgentToolResult.of(toolName, true, status,
                    String.format("数据库 %s，sensor_data 共 %d 条",
                        status.getOrDefault("database", "unknown"),
                        status.getOrDefault("sensorDataCount", 0)));
            }
            default -> AgentToolResult.of(toolName, false, null, "未知工具");
        };
    }

    private boolean containsAny(String msg, String... keywords) {
        for (String kw : keywords) {
            if (msg.contains(kw)) return true;
        }
        return false;
    }
}
