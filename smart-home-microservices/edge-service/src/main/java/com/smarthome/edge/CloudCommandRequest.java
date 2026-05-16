package com.smarthome.edge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudCommandRequest {
    
    private String deviceId;
    private String command;
    private String timestamp;
}