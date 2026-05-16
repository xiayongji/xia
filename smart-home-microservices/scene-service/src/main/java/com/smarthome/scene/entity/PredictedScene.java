package com.smarthome.scene.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictedScene {
    
    private String sceneId;
    private String sceneName;
    private double confidence;
    private LocalDateTime expectedTime;
    private String triggerReason;
}