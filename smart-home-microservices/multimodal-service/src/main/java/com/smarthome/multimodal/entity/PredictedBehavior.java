package com.smarthome.multimodal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictedBehavior {
    
    private String behaviorType;
    private double confidence;
    private String predictedAction;
    private LocalDateTime expectedTime;
}