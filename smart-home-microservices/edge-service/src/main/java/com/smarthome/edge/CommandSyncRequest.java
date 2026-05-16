package com.smarthome.edge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandSyncRequest {
    
    private String deviceId;
    private String command;
    private boolean success;
    private String message;
    private String timestamp;
}