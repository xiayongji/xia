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
public class ContextSnapshot {
    
    private String userId;
    private String location;
    private Double temperature;
    private Integer lightLevel;
    private Boolean motionDetected;
    private String lastAction;
    private LocalDateTime timestamp;
}