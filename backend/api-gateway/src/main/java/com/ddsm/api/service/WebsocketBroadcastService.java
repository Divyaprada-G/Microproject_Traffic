package com.ddsm.api.service;

import com.ddsm.common.TrafficEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebsocketBroadcastService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final ConcurrentHashMap<String, TrafficEvent> recentEvents = new ConcurrentHashMap<>();

    @KafkaListener(topics = "traffic-events", groupId = "gateway-group")
    public void consumeAndBroadcast(TrafficEvent event) {
        recentEvents.put(event.getEventId(), event);
        
        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/traffic", event);
        
        // Keep cache bounded
        if(recentEvents.size() > 1000) {
            recentEvents.clear(); // simple clear for memory safety
        }
    }

    public List<TrafficEvent> getRecentEvents() {
        return new ArrayList<>(recentEvents.values());
    }
}
