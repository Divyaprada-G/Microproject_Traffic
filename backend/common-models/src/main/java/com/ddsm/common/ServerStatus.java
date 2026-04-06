package com.ddsm.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerStatus {
    private String serverName;
    private String region;
    private String status; // ACTIVE, WARNING, FAILED
    private long lastHeartbeat;
}
