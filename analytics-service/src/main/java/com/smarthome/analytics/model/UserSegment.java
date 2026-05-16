package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSegment {
    private String segmentId;
    private String segmentName;
    private String description;
    private Double avgActivityScore;
    private String preferredTimeSlot;
    private Integer avgDevicesUsed;
    private Integer avgScenesCreated;
    private LocalDateTime lastAnalyzed;
}