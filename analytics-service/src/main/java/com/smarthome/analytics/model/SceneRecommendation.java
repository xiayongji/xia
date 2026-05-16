package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneRecommendation {
    private String sceneId;
    private String sceneName;
    private String reason;
    private Double confidence;
    private String triggerTime;
    private String context;
}