package com.ddsm.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrafficEvent {
    private String eventId;
    private String vehicleId;
    private String state;
    private String city;
    private double latitude;
    private double longitude;
    private double speed;
    private int congestionLevel;
    private long timestamp;
    private String region;
}
