package com.smarthome.scene.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "scene_rules")
public class SceneRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String ruleId;
    
    @Column(nullable = false)
    private String type; // time, device, environment
    
    @Column(columnDefinition = "TEXT")
    private String condition;
    
    @ManyToOne
    @JoinColumn(name = "scene_id")
    private Scene scene;
    
    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SceneAction> actions;
    
    @Column(nullable = false)
    private int priority = 1;
    
    private boolean enabled = true;
}