package com.smarthome.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Range {
    
    private double min;
    private double max;
    
    public boolean contains(double value) {
        return value >= min && value <= max;
    }
    
    public double getMidpoint() {
        return (min + max) / 2;
    }
    
    public double getWidth() {
        return max - min;
    }
}