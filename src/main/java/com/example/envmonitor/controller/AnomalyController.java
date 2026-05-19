package com.example.envmonitor.controller;

import com.example.envmonitor.dto.AnomalyResponse;
import com.example.envmonitor.service.AnomalyService;
import com.example.envmonitor.util.DataSourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/anomaly")
public class AnomalyController {
    private final AnomalyService anomalyService;

    public AnomalyController(AnomalyService anomalyService) {
        this.anomalyService = anomalyService;
    }

    @GetMapping("/detect")
    public AnomalyResponse detect(
        @RequestParam(defaultValue = DataSourceUtils.REAL_SERIAL) String source,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return anomalyService.detect(source, limit);
    }
}
