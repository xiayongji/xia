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
public class BehaviorEvent {
    private String deviceId;
    private String action;
    private LocalDateTime timestamp;
    private String sceneContext;
}冷