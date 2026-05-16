package com.smarthome.edge;

import com.smarthome.edge.entity.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusBatchSyncRequest {
    
    private Map<String, DeviceStatus> statusMap;
    private String syncTime;
}