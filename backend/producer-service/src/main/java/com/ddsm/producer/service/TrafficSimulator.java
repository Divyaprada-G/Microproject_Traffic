package com.ddsm.producer.service;

import com.ddsm.common.TrafficEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class TrafficSimulator {

    private static final Logger log = LoggerFactory.getLogger(TrafficSimulator.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${ingestion.url:http://localhost:8081/api/ingest/batch}")
    private String ingestionUrl;

    private final String[] states = {"Karnataka", "Delhi", "Maharashtra"};
    private final String[] cities = {"Bengaluru", "Delhi", "Mumbai"};
    private final String[] regions = {"SOUTH", "NORTH", "WEST"};
    private final double[][] coordinates = {
        {12.9716, 77.5946}, // BLR
        {28.7041, 77.1025}, // DEL
        {19.0760, 72.8777}  // MUM
    };

    @Scheduled(fixedRate = 1000) // Run every second
    public void simulateTraffic() {
        List<TrafficEvent> batch = new ArrayList<>();
        // Generate a random batch of events (simulating 10k vehicles total over time)
        int batchSize = 100 + random.nextInt(100); 

        for (int i = 0; i < batchSize; i++) {
            // Skew heavily towards Bengaluru / Karnataka
            int idx = random.nextDouble() < 0.6 ? 0 : (random.nextDouble() < 0.5 ? 1 : 2);
            
            double lat = coordinates[idx][0] + (random.nextDouble() - 0.5) * 0.1;
            double lon = coordinates[idx][1] + (random.nextDouble() - 0.5) * 0.1;
            int speed = random.nextInt(100);
            int congestion = speed < 20 ? random.nextInt(30) + 70 : (speed < 50 ? random.nextInt(40) + 30 : random.nextInt(30));

            TrafficEvent event = TrafficEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .vehicleId("KA-0" + (random.nextInt(9) + 1) + "-" + (1000 + random.nextInt(9000)))
                .state(states[idx])
                .city(cities[idx])
                .region(regions[idx])
                .latitude(lat)
                .longitude(lon)
                .speed(speed)
                .congestionLevel(congestion)
                .timestamp(System.currentTimeMillis())
                .build();
                
            batch.add(event);
        }

        try {
            restTemplate.postForObject(ingestionUrl, batch, String.class);
            log.info("Sent batch of {} traffic events to ingestion", batch.size());
        } catch (Exception e) {
            log.error("Failed to send events to ingestion: " + e.getMessage());
        }
    }
}
