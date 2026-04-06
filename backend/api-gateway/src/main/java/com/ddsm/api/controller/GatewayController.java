package com.ddsm.api.controller;

import com.ddsm.api.service.WebsocketBroadcastService;
import com.ddsm.common.ServerStatus;
import com.ddsm.common.TrafficEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.fault-detector}")
    private String faultDetectorUrl;

    @Value("${services.analytics-engine}")
    private String analyticsUrl;

    @Value("${services.storage.BLR}")
    private String blrStorageUrl;

    @Value("${services.storage.DEL}")
    private String delStorageUrl;

    @Value("${services.storage.MUM}")
    private String mumStorageUrl;

    @Autowired
    private WebsocketBroadcastService websocketService;

    @GetMapping("/dashboard/overview")
    @Cacheable(value = "dashboard-overview", key = "'overview'")
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // Fetch statuses
        List<ServerStatus> statuses = getStatuses();
        long active = statuses.stream().filter(s -> "ACTIVE".equals(s.getStatus())).count();
        long failed = statuses.stream().filter(s -> "FAILED".equals(s.getStatus())).count();
        
        // Fetch Trends
        Map<String, Object> trends = getAnalyticsTrends();

        overview.put("trafficLoad", getTotalLoad(trends));
        overview.put("activeServers", active);
        overview.put("failures", failed);
        overview.put("alerts", failed > 0 ? "Server Down! Tracking Failover" : "All Systems Normal");
        overview.put("systemHealth", failed == 0 ? "100%" : (active * 100 / (active + failed)) + "%");
        
        return overview;
    }

    @GetMapping("/traffic/live")
    public List<TrafficEvent> getLiveTraffic() {
        List<TrafficEvent> events = websocketService.getRecentEvents();
        // Return latest 50 events for initial map load
        return events.stream().limit(50).collect(Collectors.toList());
    }

    @GetMapping("/traffic/state/{state}")
    public List<TrafficEvent> getTrafficByState(@PathVariable String state) {
        // Route to the nearest/primary active server or fallback based on state
        String targetUrl = resolveStorageUrlByState(state);
        try {
            ResponseEntity<List<TrafficEvent>> response = restTemplate.exchange(
                    targetUrl + "/api/storage/state/" + state,
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<TrafficEvent>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch state data from {} - failover initiating...", targetUrl);
            // Simulating a fallback to another node if one fails
            return getFallbackData(state);
        }
    }

    @GetMapping("/servers/status")
    public List<ServerStatus> getServersStatus() {
        return getStatuses();
    }

    @GetMapping("/data/replication")
    public Map<String, String> getReplicationStats() {
        // Mocking replication stats for now since replication service doesn't expose it yet.
        Map<String, String> stats = new HashMap<>();
        stats.put("status", "Healthy");
        stats.put("BLR->DEL", "Synced");
        stats.put("DEL->MUM", "Synced");
        return stats;
    }

    @GetMapping("/faults")
    public List<ServerStatus> getFaults() {
        return getStatuses().stream()
                .filter(s -> "FAILED".equals(s.getStatus()))
                .collect(Collectors.toList());
    }

    @GetMapping("/analytics/karnataka")
    public Map<String, Object> getKarnatakaAnalytics() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(analyticsUrl + "/api/analytics/karnataka", Map.class);
            return response.getBody();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    // Helper methods
    private List<ServerStatus> getStatuses() {
        try {
            ResponseEntity<List<ServerStatus>> response = restTemplate.exchange(
                    faultDetectorUrl + "/api/health/status",
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<ServerStatus>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch statuses", e);
            return Collections.emptyList();
        }
    }

    private Map<String, Object> getAnalyticsTrends() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(analyticsUrl + "/api/analytics/trends", Map.class);
            return response.getBody();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private long getTotalLoad(Map<String, Object> trends) {
        long total = 0;
        for (Object value : trends.values()) {
            if (value instanceof Map) {
                Map<String, Object> stateStats = (Map<String, Object>) value;
                if (stateStats.containsKey("totalVehicles")) {
                    total += ((Number) stateStats.get("totalVehicles")).longValue();
                }
            }
        }
        return total;
    }
    
    // Simulating smart routing / failover rules
    private String resolveStorageUrlByState(String state) {
        List<ServerStatus> statuses = getStatuses();
        boolean blrActive = isActive(statuses, "server-blr");
        boolean delActive = isActive(statuses, "server-del");
        boolean mumActive = isActive(statuses, "server-mum");
        
        // Primary Logic
        if (state.equalsIgnoreCase("Karnataka")) {
            if (blrActive) return blrStorageUrl;
            if (delActive) return delStorageUrl; // failover
            return mumStorageUrl;
        } else if (state.equalsIgnoreCase("Delhi")) {
            if (delActive) return delStorageUrl;
            if (mumActive) return mumStorageUrl; // failover
            return blrStorageUrl;
        } else {
            if (mumActive) return mumStorageUrl;
            if (blrActive) return blrStorageUrl; // failover
            return delStorageUrl;
        }
    }
    
    private boolean isActive(List<ServerStatus> statuses, String serverName) {
        return statuses.stream().anyMatch(s -> s.getServerName().equals(serverName) && "ACTIVE".equals(s.getStatus()));
    }
    
    private List<TrafficEvent> getFallbackData(String state) {
        // Find FIRST active server and call it
        List<ServerStatus> statuses = getStatuses();
        for (ServerStatus s : statuses) {
            if ("ACTIVE".equals(s.getStatus())) {
                String nodeUrl = resolveUrlByName(s.getServerName());
                try {
                    ResponseEntity<List<TrafficEvent>> response = restTemplate.exchange(
                            nodeUrl + "/api/storage/state/" + state,
                            HttpMethod.GET, null, new ParameterizedTypeReference<List<TrafficEvent>>() {}
                    );
                    log.info("Successfully fetched data from fallback server: {}", s.getServerName());
                    return response.getBody();
                } catch (Exception e) {
                    log.error("Fallback {} also failed", s.getServerName());
                }
            }
        }
        return Collections.emptyList();
    }
    
    private String resolveUrlByName(String name) {
        switch(name) {
            case "server-blr": return blrStorageUrl;
            case "server-del": return delStorageUrl;
            case "server-mum": return mumStorageUrl;
            default: return blrStorageUrl;
        }
    }
}
