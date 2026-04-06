package com.ddsm.ingestion.controller;

import com.ddsm.common.TrafficEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);
    private static final String TOPIC = "traffic-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/batch")
    public ResponseEntity<String> ingestBatch(@RequestBody List<TrafficEvent> events) {
        if (events == null || events.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty payload");
        }
        
        events.forEach(event -> {
            // Validation
            if (event.getVehicleId() != null && !event.getVehicleId().isEmpty()) {
                kafkaTemplate.send(TOPIC, event.getRegion(), event);
            }
        });
        
        log.info("Ingested and queued {} events", events.size());
        return ResponseEntity.ok("Received");
    }
}
