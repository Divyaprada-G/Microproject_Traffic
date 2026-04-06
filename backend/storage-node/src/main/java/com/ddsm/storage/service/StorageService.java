package com.ddsm.storage.service;

import com.ddsm.common.TrafficEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StorageService {
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    
    @Value("${node.region}")
    private String region;

    @Value("${node.name}")
    private String nodeName;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentHashMap<String, TrafficEvent> localCache = new ConcurrentHashMap<>();
    private final String dataDir = "/app/data/";

    public StorageService() {
        try {
            Files.createDirectories(Paths.get(dataDir));
        } catch (IOException e) {
            log.error("Failed to create data directory", e);
        }
    }

    @KafkaListener(topics = "traffic-events", groupId = "${node.name}-group")
    public void consume(TrafficEvent event) {
        if (region.equals(event.getRegion())) {
            storeLocal(event);
        }
    }

    public void storeLocal(TrafficEvent event) {
        localCache.put(event.getEventId(), event);
        
        // Simulating JSON file storage
        File file = new File(dataDir + event.getEventId() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(mapper.writeValueAsString(event));
        } catch (IOException e) {
            log.error("Failed to write JSON file for event {}", event.getEventId(), e);
        }
    }

    public List<TrafficEvent> getAllEvents() {
        return new ArrayList<>(localCache.values());
    }
    
    public List<TrafficEvent> getEventsByState(String state) {
        return localCache.values().stream()
                .filter(e -> state.equalsIgnoreCase(e.getState()))
                .collect(Collectors.toList());
    }
    
    // For Replication logic
    public void replicateData(List<TrafficEvent> events) {
        events.forEach(this::storeLocal);
        log.info("Replicated {} events to node {}", events.size(), nodeName);
    }
}
