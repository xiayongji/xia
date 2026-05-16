package com.smarthome.device.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_shadows")
public class DeviceShadow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String deviceId;
    
    @Column(columnDefinition = "TEXT")
    private String desiredState;  // 期望状态
    
    @Column(columnDefinition = "TEXT")
    private String reportedState; // 上报状态
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    private Integer version = 1;
    
    @PrePersist
    protected void onCreate() {
        lastUpdated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
        version++;
    }
}