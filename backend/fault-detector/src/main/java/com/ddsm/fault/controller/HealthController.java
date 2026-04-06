package com.ddsm.fault.controller;

import com.ddsm.common.ServerStatus;
import com.ddsm.fault.service.HealthMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private HealthMonitorService monitorService;

    @PostMapping("/heartbeat")
    public void heartbeat(@RequestBody ServerStatus status) {
        monitorService.processHeartbeat(status);
    }

    @GetMapping("/status")
    public Collection<ServerStatus> getStatus() {
        return monitorService.getAllServerStatuses();
    }
}
