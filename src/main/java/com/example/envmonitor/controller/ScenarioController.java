package com.example.envmonitor.controller;

import com.example.envmonitor.dto.ScenarioAnalysisResponse;
import com.example.envmonitor.service.ScenarioAnalysisService;
import com.example.envmonitor.util.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scenario")
public class ScenarioController {
    private static final Logger log = LoggerFactory.getLogger(ScenarioController.class);

    private final ScenarioAnalysisService scenarioAnalysisService;

    public ScenarioController(ScenarioAnalysisService scenarioAnalysisService) {
        this.scenarioAnalysisService = scenarioAnalysisService;
    }

    @GetMapping("/analysis")
    public ResponseEntity<ScenarioAnalysisResponse> analysis(
        @RequestParam(defaultValue = "GENERAL_MONITOR") String scenario,
        @RequestParam(defaultValue = "STUDENT") String userRole,
        @RequestParam(defaultValue = DataSourceUtils.REAL_SERIAL) String source,
        @RequestParam(defaultValue = "50") int limit,
        @RequestParam(required = false) String crop,
        @RequestParam(required = false) String roomType,
        @RequestParam(required = false) Double customTempMin,
        @RequestParam(required = false) Double customTempMax,
        @RequestParam(required = false) Double customHumidityMin,
        @RequestParam(required = false) Double customHumidityMax,
        @RequestParam(required = false) Double customGasMax
    ) {
        try {
            ScenarioAnalysisResponse response = scenarioAnalysisService.analyze(
                scenario, userRole, source, limit,
                crop, roomType,
                customTempMin, customTempMax,
                customHumidityMin, customHumidityMax, customGasMax
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Scenario analysis failed", e);
            return ResponseEntity.internalServerError().body(
                new ScenarioAnalysisResponse(
                    scenario, "未知场景", userRole, "未知用户",
                    0, "ERROR", "错误", "LOW",
                    "分析过程中发生错误：" + e.getMessage(),
                    java.util.List.of(), java.util.List.of(),
                    new ScenarioAnalysisResponse.ScenarioMetricStatus("UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN"),
                    new ScenarioAnalysisResponse.ScenarioScoreBreakdown(0, 0, 0, 0, 0),
                    java.util.List.of(), source, 0, java.time.LocalDateTime.now().toString()
                )
            );
        }
    }
}
