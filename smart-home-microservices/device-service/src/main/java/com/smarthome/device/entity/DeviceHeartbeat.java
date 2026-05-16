package com.smarthome.device.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_heartbeats")
public class DeviceHeartbeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String deviceId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    private String status;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double temperature;
    private String networkStatus;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}