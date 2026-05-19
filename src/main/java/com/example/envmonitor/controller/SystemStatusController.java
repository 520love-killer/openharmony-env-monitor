package com.example.envmonitor.controller;

import com.example.envmonitor.service.SystemStatusService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemStatusController {
    private final SystemStatusService systemStatusService;

    public SystemStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping("/database-status")
    public Map<String, Object> databaseStatus() {
        return systemStatusService.databaseStatus();
    }

    @GetMapping("/retention-policy")
    public Map<String, Object> retentionPolicy() {
        return systemStatusService.retentionPolicy();
    }

    @GetMapping("/cache-status")
    public Map<String, Object> cacheStatus() {
        return systemStatusService.cacheStatus();
    }
}
