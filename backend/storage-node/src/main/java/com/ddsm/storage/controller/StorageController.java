package com.ddsm.storage.controller;

import com.ddsm.common.TrafficEvent;
import com.ddsm.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/data")
    public List<TrafficEvent> getAllData() {
        return storageService.getAllEvents();
    }
    
    @GetMapping("/state/{state}")
    public List<TrafficEvent> getDataByState(@PathVariable String state) {
        return storageService.getEventsByState(state);
    }

    @PostMapping("/replicate")
    public String replicateData(@RequestBody List<TrafficEvent> events) {
        storageService.replicateData(events);
        return "Replication Successful";
    }
}
