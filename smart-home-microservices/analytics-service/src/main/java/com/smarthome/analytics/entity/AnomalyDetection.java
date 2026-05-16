package com.smarthome.analytics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "anomaly_detection")
public class AnomalyDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String deviceId;
    
    @Column(nullable = false)
    private String deviceName;
    
    @Column(nullable = false)
    private String anomalyType;
    
    @Column(nullable = false)
    private Double confidence;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private LocalDateTime detectedAt;
    
    private LocalDateTime resolvedAt;
    
    @PrePersist
    protected void onCreate() {
        if (detectedAt == null) {
            detectedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "pending";
        }
    }
}