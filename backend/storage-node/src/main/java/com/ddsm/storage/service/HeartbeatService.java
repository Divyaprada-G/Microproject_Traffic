package com.ddsm.storage.service;

import com.ddsm.common.ServerStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HeartbeatService {
    
    @Value("${node.name}")
    private String nodeName;

    @Value("${node.region}")
    private String region;

    @Value("${fault-detector.url:http://fault-detector:8083/api/health/heartbeat}")
    private String faultDetectorUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 5000)
    public void sendHeartbeat() {
        try {
            ServerStatus status = new ServerStatus(nodeName, region, "ACTIVE", System.currentTimeMillis());
            restTemplate.postForObject(faultDetectorUrl, status, String.class);
        } catch (Exception e) {
            // Ignore if fault detector is down
        }
    }
}
