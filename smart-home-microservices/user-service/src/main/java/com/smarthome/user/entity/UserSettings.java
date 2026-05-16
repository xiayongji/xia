package com.smarthome.user.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String theme = "light";
    private String language = "zh-CN";
    private String timezone = "Asia/Shanghai";
    private boolean notificationsEnabled = true;
    private boolean emailNotifications = true;
    private boolean pushNotifications = true;
    
    @Column(columnDefinition = "TEXT")
    private String deviceGroups;
    
    @Column(columnDefinition = "TEXT")
    private String scenePreferences;
}