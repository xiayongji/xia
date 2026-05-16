package com.smarthome.multimodal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorRule {
    
    private String behaviorType;
    private String action;
    private TimeRange timeRange;
    private String location;
    private String previousAction;
    private Map<String, Object> environmentalConditions;
    private int occurrenceCount;
    private int delaySeconds;
}