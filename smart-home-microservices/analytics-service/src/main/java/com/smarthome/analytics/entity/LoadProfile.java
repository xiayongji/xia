package com.smarthome.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "load_profile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "profile_name")
    private String profileName;

    @ElementCollection
    @CollectionTable(name = "load_profile_pattern", joinColumns = @JoinColumn(name = "load_profile_id"))
    @MapKeyColumn(name = "time_slot")
    @Column(name = "power_value")
    @Builder.Default
    private Map<String, Double> historicalPattern = new HashMap<>();

    @Column(name = "avg_power")
    private Double avgPower;

    @Column(name = "peak_power")
    private Double peakPower;

    @Column(name = "min_power")
    private Double minPower;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}