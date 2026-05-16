package com.smarthome.analytics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "energy_consumption")
public class EnergyConsumption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String deviceId;
    
    @Column(nullable = false)
    private String deviceName;
    
    @Column(nullable = false)
    private Double power;
    
    @Column(nullable = false)
    private Double energy;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    private String unit = "kWh";
    private String deviceType;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}