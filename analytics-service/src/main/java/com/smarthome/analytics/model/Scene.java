package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scene implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sceneId;
    private String name;
    private String description;
    private SceneCategory category;
    private Map<String, Object> triggerConditions;
    private Map<String, Object> actions;
    private String timeSlot;
    private String userSegment;
    private Integer usageCount;
    private Double avgRating;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;

    public enum SceneCategory {
        MORNING_ROUTINE("晨起模式"),
        LEAVE_HOME("离家模式"),
        RETURN_HOME("回家模式"),
        EVENING_RELAX("晚间休息"),
        SLEEP_MODE("睡眠模式"),
        ENERGY_SAVING("节能模式"),
        ENTERTAINMENT("娱乐模式"),
        SECURITY("安防模式"),
        CUSTOM("自定义");

        private final String displayName;
        SceneCategory(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() { return displayName; }
    }
}