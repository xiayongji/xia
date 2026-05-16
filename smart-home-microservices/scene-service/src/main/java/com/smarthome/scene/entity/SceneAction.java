package com.smarthome.scene.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "scene_actions")
public class SceneAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String actionId;
    
    @Column(nullable = false)
    private String deviceId;
    
    @Column(nullable = false)
    private String command;
    
    @Column(columnDefinition = "TEXT")
    private String parameters;
    
    @Column(nullable = false)
    private int orderIndex;
    
    @ManyToOne
    @JoinColumn(name = "rule_id")
    private SceneRule rule;
    
    private String type; // device_control, notification, delay
    private int delaySeconds;
    private boolean enabled = true;
}