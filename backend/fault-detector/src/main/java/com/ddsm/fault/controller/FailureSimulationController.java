package com.ddsm.fault.controller;

import com.ddsm.fault.service.HealthMonitorService;
import com.ddsm.common.ServerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/simulate")
public class FailureSimulationController {

    @Autowired
    private HealthMonitorService healthService;

    /**
     * Endpoint to simulate node failure from an external tool.
     * Use POST /api/simulate/fail?nodeId=server-blr
     */
    @PostMapping("/fail")
    public String simulateFailure(@RequestParam String nodeId) {
        Optional<ServerStatus> status = healthService.getAllServerStatuses().stream()
                .filter(s -> s.getServerName().equals(nodeId))
                .findFirst();

        if (status.isPresent()) {
            status.get().setStatus("FAILED");
            status.get().setLastHeartbeat(System.currentTimeMillis() - 60000); // Set last heartbeat to 1 min ago
            return "SUCCESS: Node " + nodeId + " marked as FAILED in the monitor state.";
        }
        return "ERROR: Node " + nodeId + " not found.";
    }

    /**
     * Endpoint to clear simulated failure.
     */
    @PostMapping("/recover")
    public String recoverNode(@RequestParam String nodeId) {
        Optional<ServerStatus> status = healthService.getAllServerStatuses().stream()
                .filter(s -> s.getServerName().equals(nodeId))
                .findFirst();

        if (status.isPresent()) {
            status.get().setStatus("ACTIVE");
            status.get().setLastHeartbeat(System.currentTimeMillis());
            return "SUCCESS: Node " + nodeId + " marked as ACTIVE and recovered.";
        }
        return "ERROR: Node " + nodeId + " not found.";
    }
}
