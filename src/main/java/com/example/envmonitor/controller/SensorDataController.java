package com.example.envmonitor.controller;

import com.example.envmonitor.dto.SensorDataRequest;
import com.example.envmonitor.entity.SensorData;
import com.example.envmonitor.service.PredictionService;
import com.example.envmonitor.service.SensorDataService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensor-data")
public class SensorDataController {
    private static final String WAITING_MESSAGE = "暂无演示数据，请先生成模拟数据或写入后端测试数据";

    private final SensorDataService sensorDataService;
    private final PredictionService predictionService;

    public SensorDataController(SensorDataService sensorDataService, PredictionService predictionService) {
        this.sensorDataService = sensorDataService;
        this.predictionService = predictionService;
    }

    @PostMapping
    public ResponseEntity<SensorData> create(@RequestBody SensorDataRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sensorDataService.create(request));
    }

    @GetMapping("/latest")
    public Map<String, Object> latest(@RequestParam(defaultValue = SensorDataService.SOURCE_MOCK) String source) {
        String dataSource = sensorDataService.normalizeSource(source);
        return sensorDataService.latestBySource(dataSource)
            .<Map<String, Object>>map(data -> Map.of(
                "hasData", true,
                "dataSource", dataSource,
                "data", data
            ))
            .orElseGet(() -> Map.of(
                "hasData", false,
                "dataSource", dataSource,
                "message", WAITING_MESSAGE
            ));
    }

    @GetMapping("/recent")
    public List<SensorData> recent(
        @RequestParam(defaultValue = "50") int limit,
        @RequestParam(defaultValue = SensorDataService.SOURCE_MOCK) String source
    ) {
        return sensorDataService.recentBySource(source, limit);
    }

    @GetMapping
    public List<SensorData> all(@RequestParam(required = false) String source) {
        if (StringUtils.hasText(source)) {
            return sensorDataService.allBySource(source);
        }
        return sensorDataService.all();
    }

    @GetMapping("/warnings")
    public List<SensorData> warnings(@RequestParam(defaultValue = SensorDataService.SOURCE_MOCK) String source) {
        return sensorDataService.warningsBySource(source);
    }

    @GetMapping("/prediction")
    public Map<String, Object> prediction(@RequestParam(defaultValue = SensorDataService.SOURCE_MOCK) String source) {
        return predictionService.predictNext(source);
    }

    @PostMapping("/mock")
    public ResponseEntity<SensorData> mock() {
        return ResponseEntity.status(HttpStatus.CREATED).body(sensorDataService.createMockData());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
