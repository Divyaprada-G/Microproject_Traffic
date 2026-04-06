package com.ddsm.fault.service;

import com.ddsm.common.ServerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HealthMonitorService {
    private static final Logger log = LoggerFactory.getLogger(HealthMonitorService.class);
    private final ConcurrentHashMap<String, ServerStatus> servers = new ConcurrentHashMap<>();

    private static final long TIMEOUT_MS = 15000; // 15 seconds

    public void processHeartbeat(ServerStatus status) {
        servers.put(status.getServerName(), status);
    }

    @Scheduled(fixedRate = 5000)
    public void checkServerHealth() {
        long now = System.currentTimeMillis();
        for (ServerStatus server : servers.values()) {
            if (now - server.getLastHeartbeat() > TIMEOUT_MS) {
                if ("ACTIVE".equals(server.getStatus())) {
                    server.setStatus("FAILED");
                    log.warn("Server {} has FAILED (heartbeat timeout). Failover should be triggered.", server.getServerName());
                    // Real failover triggers could go to an API gateway config endpoint
                }
            } else {
                if ("FAILED".equals(server.getStatus())) {
                    server.setStatus("ACTIVE");
                    log.info("Server {} has RECOVERED.", server.getServerName());
                }
            }
        }
    }

    public Collection<ServerStatus> getAllServerStatuses() {
        // Return a mock default if empty, for the UI
        if(servers.isEmpty()) {
            verifyDefaultServers();
        }
        return servers.values();
    }
    
    private void verifyDefaultServers() {
        servers.putIfAbsent("server-blr", new ServerStatus("server-blr", "SOUTH", "FAILED", 0));
        servers.putIfAbsent("server-del", new ServerStatus("server-del", "NORTH", "FAILED", 0));
        servers.putIfAbsent("server-mum", new ServerStatus("server-mum", "WEST", "FAILED", 0));
    }
}
