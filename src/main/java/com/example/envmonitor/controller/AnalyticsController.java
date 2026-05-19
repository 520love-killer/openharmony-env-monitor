package com.example.envmonitor.controller;

import com.example.envmonitor.dto.StatisticsSummaryResponse;
import com.example.envmonitor.dto.TrendSummaryResponse;
import com.example.envmonitor.service.AnalyticsService;
import com.example.envmonitor.util.DataSourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public StatisticsSummaryResponse summary(
        @RequestParam(defaultValue = "50") int limit,
        @RequestParam(defaultValue = DataSourceUtils.REAL_SERIAL) String source
    ) {
        return analyticsService.summary(source, limit);
    }

    @GetMapping("/trend")
    public TrendSummaryResponse trend(
        @RequestParam(defaultValue = "50") int limit,
        @RequestParam(defaultValue = DataSourceUtils.REAL_SERIAL) String source
    ) {
        return analyticsService.trend(source, limit);
    }
}
