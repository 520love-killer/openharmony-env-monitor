package com.example.envmonitor.service;

import com.example.envmonitor.dto.ScenarioAnalysisResponse.ScenarioAdviceItem;
import com.example.envmonitor.dto.ScenarioAnalysisResponse.ScenarioRiskItem;
import com.example.envmonitor.service.ScenarioRuleEngine.Thresholds;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ScenarioProfileService {

    public List<ScenarioRiskItem> evaluateRisks(String scenario, Thresholds t,
                                                 double temp, double humidity, double gas,
                                                 double tempTrend, double humidityTrend) {
        List<ScenarioRiskItem> risks = new ArrayList<>();

        // Temperature risks
        if (temp > t.tempMax()) {
            String msg = switch (scenario) {
                case ScenarioRuleEngine.SCENARIO_AGRICULTURE ->
                    String.format("温度 %.1fC 超过作物适宜上限 %.1fC，高温可能影响作物生长", temp, t.tempMax());
                case ScenarioRuleEngine.SCENARIO_AC ->
                    String.format("温度 %.1fC 超过舒适范围上限 %.1fC，建议调低空调温度", temp, t.tempMax());
                case ScenarioRuleEngine.SCENARIO_INDUSTRIAL ->
                    String.format("温度 %.1fC 超过工业安全上限 %.1fC，注意设备散热", temp, t.tempMax());
                case ScenarioRuleEngine.SCENARIO_LAB ->
                    String.format("温度 %.1fC 超过实验室适宜范围 %.1fC，可能影响实验精度", temp, t.tempMax());
                default -> String.format("温度 %.1fC 超过正常范围上限 %.1fC", temp, t.tempMax());
            };
            risks.add(new ScenarioRiskItem("TEMP_HIGH", temp > t.tempMax() + 5 ? "HIGH" : "MEDIUM", msg));
        } else if (temp < t.tempMin()) {
            String msg = switch (scenario) {
                case ScenarioRuleEngine.SCENARIO_AGRICULTURE ->
                    String.format("温度 %.1fC 低于作物适宜下限 %.1fC，低温可能影响作物生长", temp, t.tempMin());
                case ScenarioRuleEngine.SCENARIO_AC ->
                    String.format("温度 %.1fC 低于舒适范围下限 %.1fC", temp, t.tempMin());
                case ScenarioRuleEngine.SCENARIO_INDUSTRIAL ->
                    String.format("温度 %.1fC 低于工业安全下限 %.1fC", temp, t.tempMin());
                case ScenarioRuleEngine.SCENARIO_LAB ->
                    String.format("温度 %.1fC 低于实验室适宜范围 %.1fC", temp, t.tempMin());
                default -> String.format("温度 %.1fC 低于正常范围下限 %.1fC", temp, t.tempMin());
            };
            risks.add(new ScenarioRiskItem("TEMP_LOW", "MEDIUM", msg));
        }

        // Humidity risks
        if (humidity > t.humidityMax()) {
            String msg = switch (scenario) {
                case ScenarioRuleEngine.SCENARIO_AGRICULTURE ->
                    String.format("湿度 %.1f%% 超过作物适宜上限 %.1f%%，高湿易引发霉菌和病害", humidity, t.humidityMax());
                case ScenarioRuleEngine.SCENARIO_AC ->
                    String.format("湿度 %.1f%% 超过舒适范围，建议开启除湿模式", humidity);
                case ScenarioRuleEngine.SCENARIO_LAB ->
                    String.format("湿度 %.1f%% 偏高，可能影响精密实验", humidity);
                default -> String.format("湿度 %.1f%% 超过正常范围上限 %.1f%%", humidity, t.humidityMax());
            };
            risks.add(new ScenarioRiskItem("HUMIDITY_HIGH", "MEDIUM", msg));
        } else if (humidity < t.humidityMin()) {
            String msg = switch (scenario) {
                case ScenarioRuleEngine.SCENARIO_AGRICULTURE ->
                    String.format("湿度 %.1f%% 低于作物适宜下限 %.1f%%，可能导致作物缺水", humidity, t.humidityMin());
                case ScenarioRuleEngine.SCENARIO_AC ->
                    String.format("湿度 %.1f%% 偏低，空气较干燥", humidity);
                default -> String.format("湿度 %.1f%% 低于正常范围下限 %.1f%%", humidity, t.humidityMin());
            };
            risks.add(new ScenarioRiskItem("HUMIDITY_LOW", "MEDIUM", msg));
        }

        // Gas risks
        if (gas >= 80) {
            risks.add(new ScenarioRiskItem("GAS_CRITICAL", "HIGH",
                String.format("燃气浓度 %.1f ppm 达到高风险水平，建议立即通风并排查", gas)));
        } else if (gas >= 50) {
            risks.add(new ScenarioRiskItem("GAS_WARNING", "HIGH",
                String.format("燃气浓度 %.1f ppm 超过安全阈值，建议通风", gas)));
        }

        // Trend risks
        if (tempTrend > 0.5) {
            risks.add(new ScenarioRiskItem("TEMP_RISING_FAST", "MEDIUM",
                String.format("温度正在快速上升（%.2fC/min），建议关注热源", tempTrend)));
        }
        if (tempTrend < -0.5) {
            risks.add(new ScenarioRiskItem("TEMP_FALLING_FAST", "LOW",
                String.format("温度正在快速下降（%.2fC/min）", tempTrend)));
        }

        return risks;
    }

    public List<ScenarioAdviceItem> generateAdvices(String scenario, Thresholds t,
                                                     double temp, double humidity, double gas,
                                                     List<ScenarioRiskItem> risks) {
        List<ScenarioAdviceItem> advices = new ArrayList<>();

        boolean hasTempHigh = risks.stream().anyMatch(r -> "TEMP_HIGH".equals(r.type()));
        boolean hasTempLow = risks.stream().anyMatch(r -> "TEMP_LOW".equals(r.type()));
        boolean hasHumidityHigh = risks.stream().anyMatch(r -> "HUMIDITY_HIGH".equals(r.type()));
        boolean hasHumidityLow = risks.stream().anyMatch(r -> "HUMIDITY_LOW".equals(r.type()));
        boolean hasGasWarning = risks.stream().anyMatch(r -> r.type().startsWith("GAS_"));

        switch (scenario) {
            case ScenarioRuleEngine.SCENARIO_AGRICULTURE -> {
                if (hasTempHigh) {
                    advices.add(new ScenarioAdviceItem("TEMP_CONTROL",
                        "加强通风降温", "建议开启遮阳网或通风口，必要时喷雾降温。"));
                }
                if (hasTempLow) {
                    advices.add(new ScenarioAdviceItem("TEMP_CONTROL",
                        "增加保温", "建议检查夜间保温措施，必要时增加覆盖物。"));
                }
                if (hasHumidityHigh) {
                    advices.add(new ScenarioAdviceItem("HUMIDITY_CONTROL",
                        "加强通风防霉", "建议加大通风量，防止霉菌和病害滋生。"));
                }
                if (hasHumidityLow) {
                    advices.add(new ScenarioAdviceItem("HUMIDITY_CONTROL",
                        "适当增湿", "建议喷雾或浇水增湿，保持土壤水分。"));
                }
                if (!hasTempHigh && !hasTempLow && !hasHumidityHigh && !hasHumidityLow) {
                    advices.add(new ScenarioAdviceItem("GENERAL",
                        "环境适宜", "当前温湿度基本适合作物生长，继续保持观察。"));
                }
            }
            case ScenarioRuleEngine.SCENARIO_AC -> {
                if (hasTempHigh) {
                    advices.add(new ScenarioAdviceItem("AC_CONTROL",
                        "调低空调温度", "建议将空调设为 26C 左右，中高档风速。"));
                    advices.add(new ScenarioAdviceItem("AC_CONTROL",
                        "检查门窗", "确保门窗关闭，减少外部热空气进入。"));
                }
                if (hasHumidityHigh) {
                    advices.add(new ScenarioAdviceItem("AC_CONTROL",
                        "开启除湿模式", "建议切换空调至除湿模式，降低室内湿度。"));
                }
                if (hasHumidityLow) {
                    advices.add(new ScenarioAdviceItem("AC_CONTROL",
                        "适当加湿", "建议减少长时间除湿，或适当加湿。"));
                }
                if (!hasTempHigh && !hasHumidityHigh && !hasHumidityLow) {
                    advices.add(new ScenarioAdviceItem("GENERAL",
                        "环境舒适", "当前室内温湿度较舒适，可维持当前空调设置。"));
                }
            }
            case ScenarioRuleEngine.SCENARIO_INDUSTRIAL -> {
                if (hasGasWarning) {
                    advices.add(new ScenarioAdviceItem("SAFETY",
                        "立即通风", "燃气异常，请立即开启通风系统，远离火源。"));
                    advices.add(new ScenarioAdviceItem("SAFETY",
                        "排查气源", "检查是否有燃气泄漏或异常气体来源。"));
                }
                if (hasTempHigh) {
                    advices.add(new ScenarioAdviceItem("SAFETY",
                        "检查设备散热", "温度过高，请检查设备散热系统是否正常工作。"));
                }
                if (!hasGasWarning && !hasTempHigh) {
                    advices.add(new ScenarioAdviceItem("GENERAL",
                        "环境正常", "当前环境指标在安全范围内，可继续正常运行。"));
                }
            }
            case ScenarioRuleEngine.SCENARIO_LAB -> {
                if (hasTempHigh || hasTempLow) {
                    advices.add(new ScenarioAdviceItem("LAB_CONTROL",
                        "关注温度稳定性", "温度偏离适宜范围，可能影响实验精度。"));
                }
                if (hasHumidityHigh || hasHumidityLow) {
                    advices.add(new ScenarioAdviceItem("LAB_CONTROL",
                        "关注湿度影响", "湿度偏离适宜范围，注意对实验条件的影响。"));
                }
                if (!hasTempHigh && !hasTempLow && !hasHumidityHigh && !hasHumidityLow) {
                    advices.add(new ScenarioAdviceItem("GENERAL",
                        "实验条件适宜", "当前环境条件较适合实验，建议持续监测。"));
                }
            }
            default -> {
                if (hasTempHigh) {
                    advices.add(new ScenarioAdviceItem("TEMP_CONTROL",
                        "注意高温", "温度偏高，建议检查通风或制冷设备。"));
                }
                if (hasTempLow) {
                    advices.add(new ScenarioAdviceItem("TEMP_CONTROL",
                        "注意低温", "温度偏低，建议检查保温措施。"));
                }
                if (hasHumidityHigh) {
                    advices.add(new ScenarioAdviceItem("HUMIDITY_CONTROL",
                        "注意高湿", "湿度偏高，建议开启除湿或通风。"));
                }
                if (hasHumidityLow) {
                    advices.add(new ScenarioAdviceItem("HUMIDITY_CONTROL",
                        "注意低湿", "湿度偏低，建议适当增湿。"));
                }
                if (hasGasWarning) {
                    advices.add(new ScenarioAdviceItem("SAFETY",
                        "燃气异常", "燃气浓度异常，建议通风并排查。"));
                }
                if (risks.isEmpty()) {
                    advices.add(new ScenarioAdviceItem("GENERAL",
                        "环境正常", "当前环境指标正常，继续保持。"));
                }
            }
        }

        return advices;
    }

    public String generateSummary(String scenario, int score, String levelName,
                                   List<ScenarioRiskItem> risks, int sampleCount) {
        String base = switch (scenario) {
            case ScenarioRuleEngine.SCENARIO_AGRICULTURE -> "温室环境";
            case ScenarioRuleEngine.SCENARIO_AC -> "室内环境";
            case ScenarioRuleEngine.SCENARIO_INDUSTRIAL -> "工业环境";
            case ScenarioRuleEngine.SCENARIO_LAB -> "实验室环境";
            default -> "环境";
        };

        if (risks.isEmpty()) {
            return String.format("当前%s整体%s，基于最近 %d 条数据评估。", base, levelName, sampleCount);
        }

        long highRisks = risks.stream().filter(r -> "HIGH".equals(r.level())).count();
        if (highRisks > 0) {
            return String.format("当前%s存在 %d 项高风险，综合评分 %d 分（%s），基于最近 %d 条数据。",
                base, highRisks, score, levelName, sampleCount);
        }

        return String.format("当前%s基本%s，存在 %d 项需要注意的风险，综合评分 %d 分，基于最近 %d 条数据。",
            base, levelName, risks.size(), score, sampleCount);
    }
}
