package com.smarthome.scene.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.scene.entity.Scene;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SceneRequest {
    private String name;
    private String description;
    private Long userId;
    private String triggerType;
    private String triggerConditions;
    private Object triggerTime;
    private Boolean enabled;
    private Integer priority;
    private List<Map<String, Object>> actions;

    public Scene toEntity() {
        Scene scene = new Scene();
        scene.setName(name);
        scene.setDescription(description);
        scene.setUserId(userId != null ? userId : 1L);
        scene.setTriggerType(triggerType != null ? triggerType : "manual");
        scene.setTriggerConditions(triggerConditions);
        scene.setEnabled(enabled != null ? enabled : true);
        scene.setPriority(priority != null ? priority : 0);
        if (actions != null && !actions.isEmpty()) {
            try {
                scene.setActions(new ObjectMapper().writeValueAsString(actions));
            } catch (Exception ignored) {
                scene.setActions("[]");
            }
        }
        return scene;
    }
}
