package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyAnalysisReport {
    private String deviceId;
    private LocalDateTime reportTime;
    private Double totalEnergy;
    private Double avgDailyEnergy;
    private Double peakPower;
    private Double costEstimate;
    private String efficiencyGrade;
    private List<String> suggestions;
    private CompareResult comparedToLastPeriod;
}