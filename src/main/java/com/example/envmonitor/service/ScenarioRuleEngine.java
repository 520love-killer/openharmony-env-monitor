package com.example.envmonitor.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ScenarioRuleEngine {

    public static final String SCENARIO_GENERAL = "GENERAL_MONITOR";
    public static final String SCENARIO_AGRICULTURE = "AGRICULTURE_GREENHOUSE";
    public static final String SCENARIO_AC = "SMART_AIR_CONDITIONER";
    public static final String SCENARIO_INDUSTRIAL = "INDUSTRIAL_SAFETY";
    public static final String SCENARIO_LAB = "LAB_ENVIRONMENT";
    public static final String SCENARIO_CUSTOM = "CUSTOM_SCENARIO";

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_FARMER = "FARMER";
    public static final String ROLE_ENGINEER = "ENGINEER";
    public static final String ROLE_AC_USER = "AC_USER";
    public static final String ROLE_RESEARCHER = "RESEARCHER";
    public static final String ROLE_CUSTOM = "CUSTOM";

    public static String scenarioName(String scenario) {
        return switch (scenario) {
            case SCENARIO_GENERAL -> "通用环境监测";
            case SCENARIO_AGRICULTURE -> "农业温室";
            case SCENARIO_AC -> "智能空调";
            case SCENARIO_INDUSTRIAL -> "工业安全";
            case SCENARIO_LAB -> "实验室环境";
            case SCENARIO_CUSTOM -> "自定义场景";
            default -> "通用环境监测";
        };
    }

    public static String roleName(String role) {
        return switch (role) {
            case ROLE_STUDENT -> "学生 / 实验学习者";
            case ROLE_FARMER -> "农业种植者";
            case ROLE_ENGINEER -> "工程技术人员";
            case ROLE_AC_USER -> "空调使用者";
            case ROLE_RESEARCHER -> "研究分析者";
            case ROLE_CUSTOM -> "自定义用户";
            default -> "学生 / 实验学习者";
        };
    }

    public static double[] weights(String scenario) {
        return switch (scenario) {
            case SCENARIO_AGRICULTURE -> new double[]{0.35, 0.30, 0.15, 0.10, 0.10};
            case SCENARIO_AC -> new double[]{0.40, 0.20, 0.10, 0.25, 0.05};
            case SCENARIO_INDUSTRIAL -> new double[]{0.25, 0.10, 0.40, 0.05, 0.20};
            case SCENARIO_LAB -> new double[]{0.20, 0.15, 0.10, 0.25, 0.30};
            default -> new double[]{0.30, 0.25, 0.25, 0.10, 0.10};
        };
    }

    public static Thresholds thresholds(String scenario, String crop, String roomType,
                                         Double customTempMin, Double customTempMax,
                                         Double customHumidityMin, Double customHumidityMax,
                                         Double customGasMax) {
        if (SCENARIO_CUSTOM.equals(scenario)) {
            return new Thresholds(
                customTempMin != null ? customTempMin : 18.0,
                customTempMax != null ? customTempMax : 35.0,
                customHumidityMin != null ? customHumidityMin : 30.0,
                customHumidityMax != null ? customHumidityMax : 75.0,
                customGasMax != null ? customGasMax : 50.0
            );
        }

        return switch (scenario) {
            case SCENARIO_AGRICULTURE -> agricultureThresholds(crop);
            case SCENARIO_AC -> acThresholds(roomType);
            case SCENARIO_INDUSTRIAL -> new Thresholds(10.0, 35.0, 20.0, 80.0, 50.0);
            case SCENARIO_LAB -> new Thresholds(20.0, 28.0, 40.0, 65.0, 50.0);
            default -> new Thresholds(18.0, 35.0, 30.0, 75.0, 50.0);
        };
    }

    private static Thresholds agricultureThresholds(String crop) {
        return switch (crop) {
            case "tomato" -> new Thresholds(20.0, 30.0, 50.0, 70.0, 50.0);
            case "strawberry" -> new Thresholds(15.0, 25.0, 60.0, 80.0, 50.0);
            case "cucumber" -> new Thresholds(22.0, 32.0, 60.0, 85.0, 50.0);
            default -> new Thresholds(18.0, 30.0, 50.0, 80.0, 50.0);
        };
    }

    private static Thresholds acThresholds(String roomType) {
        return switch (roomType) {
            case "dormitory" -> new Thresholds(25.0, 27.0, 40.0, 60.0, 50.0);
            case "classroom" -> new Thresholds(24.0, 26.0, 40.0, 60.0, 50.0);
            case "bedroom" -> new Thresholds(25.0, 27.0, 40.0, 60.0, 50.0);
            case "office" -> new Thresholds(24.0, 26.0, 40.0, 60.0, 50.0);
            default -> new Thresholds(24.0, 27.0, 40.0, 60.0, 50.0);
        };
    }

    public static String levelFromScore(int score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 75) return "GOOD";
        if (score >= 60) return "NORMAL";
        return "WARNING";
    }

    public static String levelNameFromScore(int score) {
        if (score >= 90) return "优秀";
        if (score >= 75) return "良好";
        if (score >= 60) return "一般";
        return "预警";
    }

    public record Thresholds(double tempMin, double tempMax,
                             double humidityMin, double humidityMax,
                             double gasMax) {
    }
}
