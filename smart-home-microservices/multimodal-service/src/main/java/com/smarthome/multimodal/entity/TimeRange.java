package com.smarthome.multimodal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRange {
    
    private LocalTime start;
    private LocalTime end;
    
    public boolean contains(LocalTime time) {
        return !time.isBefore(start) && !time.isAfter(end);
    }
}