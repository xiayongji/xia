package com.smarthome.edge.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandResult {
    
    private String deviceId;
    private boolean success;
    private String message;
    private String response;
    private LocalDateTime timestamp;
}