package com.smarthome.scene.service;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SceneExecutionContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long sceneId;
    private String sceneName;
    private Long userId;
    private String triggerType;
    private String triggerSource;
    private LocalDateTime triggerTime;
    private Map<String, Object> deviceStates;
    private Map<String, Object> sensorData;
    private Map<String, Object> userContext;
    private Boolean conditionsMet;
    private Boolean executionAllowed;
    private String executionResult;
    private Map<String, Object> executionData;

    public SceneExecutionContext() {
        this.triggerTime = LocalDateTime.now();
        this.conditionsMet = false;
        this.executionAllowed = true;
    }

    public void addDeviceState(String deviceId, Object state) {
        if (this.deviceStates == null) {
            this.deviceStates = new java.util.HashMap<>();
        }
        this.deviceStates.put(deviceId, state);
    }

    public void addSensorData(String sensorId, Object data) {
        if (this.sensorData == null) {
            this.sensorData = new java.util.HashMap<>();
        }
        this.sensorData.put(sensorId, data);
    }

    public void addUserContext(String key, Object value) {
        if (this.userContext == null) {
            this.userContext = new java.util.HashMap<>();
        }
        this.userContext.put(key, value);
    }
}