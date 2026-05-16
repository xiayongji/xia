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
public class BehaviorPattern {
    private String patternId;
    private String patternName;
    private PatternType type;
    private List<String> triggerDevices;
    private List<String> actionDevices;
    private Double occurrenceProbability;
    private Integer totalOccurrences;
    private String timeSlot;
    private String contextCondition;
    private LocalDateTime lastDetected;
}

enum PatternType {
    ROUTINE("常规行为"),
    CONTEXT_AWARE("场景感知"),
    PREDICTED("预测行为"),
    ANOMALY("异常行为");

    private final String name;

    PatternType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}