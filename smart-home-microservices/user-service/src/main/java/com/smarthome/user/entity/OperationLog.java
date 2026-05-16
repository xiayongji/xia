package com.smarthome.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_logs")
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String operation;
    
    @Column(nullable = false)
    private String resource;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    private String ipAddress;
    private String userAgent;
    
    @Column(columnDefinition = "TEXT")
    private String requestData;
    
    @Column(columnDefinition = "TEXT")
    private String responseData;
    
    @Column(nullable = false)
    private boolean success = true;
    
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}