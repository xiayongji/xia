package com.smarthome.scene.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scene_executions")
public class SceneExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sceneId;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    private String status; // running, success, failed
    
    private String triggerType;
    private String triggerCondition;
    
    @Column(columnDefinition = "TEXT")
    private String executionLog;
    
    private int totalActions;
    private int successActions;
    private int failedActions;
    
    @PrePersist
    protected void onCreate() {
        startTime = LocalDateTime.now();
        status = "running";
    }
}