package com.ddsm.edge.controller;

import com.ddsm.common.TrafficEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/edge")
public class EdgeController {

    private static final Logger log = LoggerFactory.getLogger(EdgeController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    
    // In-memory set to remove duplicate vehicleIds
    private final Set<String> vehicleCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Value("${EDGE_NODE_ENABLED:false}")
    private boolean enabled;

    @Value("${ingestion.url:http://ingestion:8081/api/ingest/batch}")
    private String ingestionUrl;

    @PostMapping("/events")
    public String processEvents(@RequestBody List<TrafficEvent> events) {
        if (!enabled) {
            log.info("Request received but Edge Mode is FALSE. Data not forwarded.");
            return "SUCCESS: Request received (Edge processing disabled)";
        }

        log.info("Edge Node processing batch of {} events...", events.size());

        // Deduplication based on vehicleId
        List<TrafficEvent> uniqueEvents = new ArrayList<>();
        for (TrafficEvent event : events) {
            if (vehicleCache.add(event.getVehicleId())) {
                uniqueEvents.add(event);
            }
        }

        // Automatic periodic cache cleanup simulation
        if (vehicleCache.size() > 20000) { vehicleCache.clear(); }

        try {
            log.info("Forwarding {} unique events to: {}", uniqueEvents.size(), ingestionUrl);
            restTemplate.postForObject(ingestionUrl, uniqueEvents, String.class);
            return "SUCCESS: " + uniqueEvents.size() + " unique events forwarded to ingestion.";
        } catch (Exception e) {
            log.error("Failed to forward payload: " + e.getMessage());
            return "ERROR: Internal communication failure.";
        }
    }
}
