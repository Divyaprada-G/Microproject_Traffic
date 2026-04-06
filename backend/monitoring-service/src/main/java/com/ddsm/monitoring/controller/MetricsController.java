package com.ddsm.monitoring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MetricsController {

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("messagesProcessed", 1200);
        metrics.put("activeNodes", 3);
        metrics.put("replicationEvents", 25);
        metrics.put("failoverEvents", 1);
        metrics.put("systemStatus", "HEALTHY");
        return metrics;
    }
}
