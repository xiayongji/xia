package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyStat {
    private String deviceId;
    private LocalDate date;
    private Double totalEnergy;
    private Double avgPower;
    private Double maxPower;
    private Double minPower;
    private Integer usageCount;
    private List<EnergyRecord> records;
    private Double cost;
    private Double comparedToYesterday;
    private Double comparedToLastWeek;
}