package com.ddsm.analytics.service;

import com.ddsm.common.TrafficEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
    
    // state -> List<event>
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, TrafficEvent>> stateData = new ConcurrentHashMap<>();

    public AnalyticsService() {
        stateData.put("Karnataka", new ConcurrentHashMap<>());
        stateData.put("Delhi", new ConcurrentHashMap<>());
        stateData.put("Maharashtra", new ConcurrentHashMap<>());
    }

    @KafkaListener(topics = "traffic-events", groupId = "analytics-group")
    public void consume(TrafficEvent event) {
        stateData.computeIfAbsent(event.getState(), k -> new ConcurrentHashMap<>())
                 .put(event.getEventId(), event);
    }

    public Map<String, Object> getStateTrends() {
        Map<String, Object> trends = new HashMap<>();
        stateData.forEach((state, events) -> {
            long totalEvents = events.size();
            double avgSpeed = events.values().stream().mapToDouble(TrafficEvent::getSpeed).average().orElse(0.0);
            double avgCongestion = events.values().stream().mapToDouble(TrafficEvent::getCongestionLevel).average().orElse(0.0);
            
            Map<String, Object> stateStats = new HashMap<>();
            stateStats.put("totalVehicles", totalEvents);
            stateStats.put("averageSpeed", avgSpeed);
            stateStats.put("averageCongestion", avgCongestion);
            trends.put(state, stateStats);
        });
        return trends;
    }

    public Map<String, Object> getKarnatakaInsights() {
        return (Map<String, Object>) getStateTrends().getOrDefault("Karnataka", new HashMap<>());
    }
}
