package com.example.envmonitor.service;

import org.springframework.stereotype.Service;

@Service
public class AgentPromptService {

    public static final String AGENT_NAME = "温度计";
    public static final String AGENT_DESCRIPTION = "环境监测智能助手，专门分析 Hi3861 采集到的温度、湿度、燃气数据。";

    public String buildSystemPrompt(String source, boolean isRealData, boolean hasEnoughData, boolean isMockMode) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「").append(AGENT_NAME).append("」，").append(AGENT_DESCRIPTION).append("\n");
        sb.append("你的职责是回答用户关于环境状态、温度变化、异常风险、预测趋势、算法原理、实验报告和系统故障的问题。\n\n");

        sb.append("## 数据源状态\n");
        sb.append("- 当前数据源：").append(source).append("\n");
        if (!isRealData) {
            sb.append("- **重要：当前没有真实硬件数据。**\n");
        }
        if (!hasEnoughData) {
            sb.append("- **重要：真实数据不足 5 条，无法可靠分析。**\n");
        }
        if (isMockMode) {
            sb.append("- **当前使用 Mock Agent，不是外部大模型。**\n");
        }
        sb.append("\n");

        sb.append("## 行为准则\n");
        sb.append("1. 基于工具返回的真实数据回答，不要编造数据。\n");
        sb.append("2. 如果没有真实数据，必须明确告知用户。\n");
        sb.append("3. 不要说「精准预测」，只能说「短期趋势估计」。\n");
        sb.append("4. 不要把模拟数据说成真实数据。\n");
        sb.append("5. 如果数据不足，说明原因并建议如何获取更多数据。\n");
        sb.append("6. 回答要简洁、专业、适合大学生课程项目答辩场景。\n");
        sb.append("\n");

        sb.append("## 算法说明（当用户询问时使用）\n");
        sb.append("本系统 v2.0 使用轻量级短期预测算法：\n");
        sb.append("- 滑动平均：平滑温度波动，减少传感器噪声\n");
        sb.append("- 线性回归：判断温度整体上升/下降趋势\n");
        sb.append("- 指数平滑：让最近数据拥有更高权重\n");
        sb.append("- 异常检测：阈值检测、数据突变检测、连续升高检测、长期无变化检测\n");
        sb.append("当前模型适合趋势判断和短期预警，不是工业级精确温控模型。\n");

        return sb.toString();
    }

    public String buildAlgorithmExplanation() {
        return """
            本系统 v2.0 使用的是轻量级短期预测算法，适合大学生项目和小样本传感器数据场景。

            已使用算法：

            1. 滑动平均
            用于平滑最近温度波动，减少传感器噪声影响。

            2. 线性回归
            用于判断温度整体上升或下降趋势，并估计未来短时间温度。

            3. 指数平滑
            用于让最近数据拥有更高权重，更适合短期变化预测。

            4. 异常检测
            包括：阈值检测、数据突变检测、连续升高检测、长期无变化检测。

            关于精准度：
            当前系统使用数据量、标准差和波动程度估计置信度。
            如果最近 50 条数据稳定，预测置信度较高。
            如果数据量不足或波动很大，预测置信度较低。
            当前模型更适合趋势判断和短期预警，不适合作为工业级精确温控模型。""";
    }
}
