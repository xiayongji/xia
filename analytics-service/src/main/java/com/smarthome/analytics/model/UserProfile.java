package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private Long userId;
    private String nickname;
    private UserSegmentType segmentType;
    private Double activityLevel;
    private String lifePattern;
    private Map<String, Integer> deviceUsageCount;
    private Map<String, Integer> sceneUsageCount;
    private String preferredTemperature;
    private String preferredBrightness;
    private Boolean isEnergyConscious;
    private Double comfortPriority;
    private LocalDateTime lastActiveTime;
    private LocalDateTime profileUpdatedTime;
}