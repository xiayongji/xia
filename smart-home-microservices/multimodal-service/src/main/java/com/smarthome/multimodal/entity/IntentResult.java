package com.smarthome.multimodal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentResult {
    
    private String intent;
    private IntentType intentType;
    private double confidence;
    private String targetDevice;
    private InputType inputType;
    private String rawInput;
}