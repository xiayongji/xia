package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResult {
    private String sceneId;
    private String sceneName;
    private String reason;
    private Double score;
    private String algorithm;
    private List<String> tags;
    private String segmentMatch;
    private Integer rank;
}