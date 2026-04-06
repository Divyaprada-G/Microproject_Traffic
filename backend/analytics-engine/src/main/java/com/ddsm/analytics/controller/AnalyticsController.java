package com.ddsm.analytics.controller;

import com.ddsm.analytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/trends")
    public Map<String, Object> getTrends() {
        return analyticsService.getStateTrends();
    }

    @GetMapping("/karnataka")
    public Map<String, Object> getKarnatakaInsights() {
        return analyticsService.getKarnatakaInsights();
    }
}
