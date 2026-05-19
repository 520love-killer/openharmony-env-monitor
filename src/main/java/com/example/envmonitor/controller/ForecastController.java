package com.example.envmonitor.controller;

import com.example.envmonitor.dto.ForecastResponse;
import com.example.envmonitor.service.ForecastService;
import com.example.envmonitor.util.DataSourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forecast")
public class ForecastController {
    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping("/temperature")
    public ForecastResponse temperature(
        @RequestParam(defaultValue = DataSourceUtils.REAL_SERIAL) String source,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return forecastService.temperatureForecast(source, limit);
    }
}
