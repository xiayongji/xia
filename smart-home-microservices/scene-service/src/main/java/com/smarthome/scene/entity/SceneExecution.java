package com.smarthome.scene.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "scene_executions")
public class SceneExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scene_id")
    private Long sceneId;

    @Column(name = "trigger_type")
    private String triggerType;

    @Column(name = "trigger_source")
    private String triggerSource;

    @Column
    private String status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "actions_executed")
    private Integer actionsExecuted;

    @Column(name = "actions_total")
    private Integer actionsTotal;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        startTime = LocalDateTime.now();
        if (status == null) status = "RUNNING";
    }
}