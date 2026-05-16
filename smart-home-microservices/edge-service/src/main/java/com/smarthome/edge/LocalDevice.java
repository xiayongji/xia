package com.smarthome.edge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalDevice {
    
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String protocol;
    private String status;
    private long lastHeartbeatTime;
}