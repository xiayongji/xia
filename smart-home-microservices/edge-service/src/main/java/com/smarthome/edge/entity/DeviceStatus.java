package com.smarthome.edge.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "edge_device_status", indexes = {
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_last_update", columnList = "last_update_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "device_type", length = 64)
    private String deviceType;

    @Column(name = "power")
    private Double power;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "properties", columnDefinition = "TEXT")
    private String properties;

    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (lastUpdateTime == null) {
            lastUpdateTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdateTime = LocalDateTime.now();
    }
}
