package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSceneInteraction implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String sceneId;
    private LocalDateTime timestamp;
    private InteractionType type;
    private Integer durationSeconds;
    private Boolean completed;
    private String context;

    public enum InteractionType {
        VIEW,
        TRIGGER,
        EDIT,
        FAVORITE,
        SKIP,
        COMPLETE
    }
}