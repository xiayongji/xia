package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareResult {
    private Double current;
    private Double previous;
    private Double changeRate;
    private String trend;
    private String description;
}