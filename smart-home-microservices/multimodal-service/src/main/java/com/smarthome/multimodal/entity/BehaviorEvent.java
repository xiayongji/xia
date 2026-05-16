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
public class BehaviorEvent {
    
    private String userId;
    private String behaviorType;
    private String action;
    private String location;
    private String previousAction;
    private LocalDateTime timestamp;
}