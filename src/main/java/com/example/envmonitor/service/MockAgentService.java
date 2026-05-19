package com.example.envmonitor.service;

import com.example.envmonitor.dto.AgentAnalyzeRequest;
import com.example.envmonitor.dto.AgentAnalyzeResponse;
import com.example.envmonitor.dto.AnomalyResponse;
import com.example.envmonitor.dto.ForecastResponse;
import com.example.envmonitor.dto.StatisticsSummaryResponse;
import com.example.envmonitor.util.DataSourceUtils;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MockAgentService {
    private final AnalyticsService analyticsService;
    private final ForecastService forecastService;
    private final AnomalyService anomalyService;

    public MockAgentService(AnalyticsService analyticsService, ForecastService forecastService, AnomalyService anomalyService) {
        this.analyticsService = analyticsService;
        this.forecastService = forecastService;
        this.anomalyService = anomalyService;
    }

    public AgentAnalyzeResponse analyze(AgentAnalyzeRequest request) {
        String source = request == null || request.source() == null ? DataSourceUtils.REAL_SERIAL : request.source();
        StatisticsSummaryResponse summary = analyticsService.summary(source, 50);
        ForecastResponse forecast = forecastService.temperatureForecast(source, 50);
        AnomalyResponse anomalies = anomalyService.detect(source, 50);

        if (!summary.success()) {
            return new AgentAnalyzeResponse(false,
                "当前真实数据不足，无法生成可靠环境分析。请先接入 Hi3861 串口或 MQTT 真实数据，或明确切换到 MOCK 演示。",
                List.of("statisticsSummary", "temperatureForecast", "anomalyDetection"));
        }

        StringBuilder answer = new StringBuilder();
        answer.append("当前环境分析基于 ").append(summary.source()).append(" 数据。");
        answer.append("最近 ").append(summary.sampleCount()).append(" 条数据平均温度为 ")
            .append(summary.temperatureAvg()).append("℃，平均湿度为 ").append(summary.humidityAvg())
            .append("%，燃气最大值为 ").append(summary.gasMax()).append("ppm。");
        if (forecast.success()) {
            answer.append("未来 10 分钟温度预测为 ").append(forecast.finalForecast10min())
                .append("℃，趋势为 ").append(forecast.trend()).append("，置信度 ").append(forecast.confidence()).append("。");
        }
        if (anomalies.hasAnomaly()) {
            answer.append("异常检测发现 ").append(anomalies.anomalyCount()).append(" 个风险，优先处理高等级异常。");
        } else {
            answer.append("异常检测暂未发现明显风险。");
        }
        answer.append("当前 Mock Agent 仅基于统计、预测和异常检测接口结果生成说明，不调用外部大模型。");
        return new AgentAnalyzeResponse(true, answer.toString(), List.of("statisticsSummary", "temperatureForecast", "anomalyDetection"));
    }
}
