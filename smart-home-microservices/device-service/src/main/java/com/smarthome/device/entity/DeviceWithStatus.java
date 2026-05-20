package com.smarthome.device.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceWithStatus {

    private Long id;
    private String deviceId;
    private String name;
    private String type;
    private String protocol;
    private String status;
    private String ipAddress;
    private String macAddress;
    private String firmwareVersion;
    private String manufacturer;
    private String model;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastHeartbeat;
    private Double power;
    private Double temperature;
    private Double humidity;
    private String properties;
    private String statusUpdateTime;
}