package com.ddsm.replication.service;

import com.ddsm.common.TrafficEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReplicationService {
    private static final Logger log = LoggerFactory.getLogger(ReplicationService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${storage.delhi.url:http://server-del:8080/api/storage/replicate}")
    private String delhiUrl;

    @Value("${storage.mumbai.url:http://server-mum:8080/api/storage/replicate}")
    private String mumbaiUrl;

    private final ConcurrentHashMap<String, List<TrafficEvent>> queues = new ConcurrentHashMap<>();

    public ReplicationService() {
        queues.put("DELHI", new ArrayList<>());
        queues.put("MUMBAI", new ArrayList<>());
    }

    @KafkaListener(topics = "traffic-events", groupId = "replication-group")
    public void consume(TrafficEvent event) {
        // Replicate: Bengaluru -> Delhi, Delhi -> Mumbai
        if ("SOUTH".equals(event.getRegion())) { // Bengaluru
            queues.get("DELHI").add(event);
        } else if ("NORTH".equals(event.getRegion())) { // Delhi
            queues.get("MUMBAI").add(event);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void pushReplication() {
        pushToNode("DELHI", delhiUrl);
        pushToNode("MUMBAI", mumbaiUrl);
    }

    private void pushToNode(String target, String url) {
        List<TrafficEvent> batch = new ArrayList<>(queues.get(target));
        if (batch.isEmpty()) return;

        try {
            restTemplate.postForObject(url, batch, String.class);
            queues.get(target).removeAll(batch);
            log.info("Successfully replicated {} records to {}", batch.size(), target);
        } catch (Exception e) {
            log.warn("Failed to replicate to {}: {}", target, e.getMessage());
        }
    }
}
