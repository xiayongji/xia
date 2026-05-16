package com.smarthome.device.service;

import com.smarthome.device.entity.DeviceShadow;
import com.smarthome.device.repository.DeviceShadowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceShadowService {

    private final DeviceShadowRepository shadowRepository;
    private final ObjectMapper objectMapper;

    /**
     * 获取设备影子
     */
    public Optional<DeviceShadow> getShadow(String deviceId) {
        return shadowRepository.findByDeviceId(deviceId);
    }

    /**
     * 创建或更新设备影子
     */
    public DeviceShadow saveShadow(String deviceId, String desiredState, String reportedState) {
        DeviceShadow shadow = shadowRepository.findByDeviceId(deviceId)
                .orElseGet(() -> {
                    DeviceShadow newShadow = new DeviceShadow();
                    newShadow.setDeviceId(deviceId);
                    return newShadow;
                });

        shadow.setDesiredState(desiredState);
        shadow.setReportedState(reportedState);
        shadow.setLastUpdated(LocalDateTime.now());

        DeviceShadow savedShadow = shadowRepository.save(shadow);
        
        log.info("设备影子更新: {}", deviceId);
        return savedShadow;
    }

    /**
     * 更新期望状态
     */
    public void updateDesiredState(String deviceId, Map<String, Object> desiredState) {
        try {
            String desiredStateJson = objectMapper.writeValueAsString(desiredState);
            
            DeviceShadow shadow = shadowRepository.findByDeviceId(deviceId)
                    .orElseGet(() -> {
                        DeviceShadow newShadow = new DeviceShadow();
                        newShadow.setDeviceId(deviceId);
                        return newShadow;
                    });

            shadow.setDesiredState(desiredStateJson);
            shadow.setLastUpdated(LocalDateTime.now());

            shadowRepository.save(shadow);

            log.info("设备期望状态更新: {}, state: {}", deviceId, desiredStateJson);
            
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败: {}", e.getMessage());
            throw new RuntimeException("状态序列化失败", e);
        }
    }

    /**
     * 更新上报状态
     */
    public void updateReportedState(String deviceId, Map<String, Object> reportedState) {
        try {
            String reportedStateJson = objectMapper.writeValueAsString(reportedState);
            
            DeviceShadow shadow = shadowRepository.findByDeviceId(deviceId)
                    .orElseGet(() -> {
                        DeviceShadow newShadow = new DeviceShadow();
                        newShadow.setDeviceId(deviceId);
                        return newShadow;
                    });

            shadow.setReportedState(reportedStateJson);
            shadow.setLastUpdated(LocalDateTime.now());

            shadowRepository.save(shadow);

            log.info("设备上报状态更新: {}, state: {}", deviceId, reportedStateJson);
            
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败: {}", e.getMessage());
            throw new RuntimeException("状态序列化失败", e);
        }
    }

    /**
     * 获取期望状态
     */
    public Map<String, Object> getDesiredState(String deviceId) {
        return shadowRepository.findByDeviceId(deviceId)
                .map(shadow -> parseJsonToMap(shadow.getDesiredState()))
                .orElse(Map.of());
    }

    /**
     * 获取上报状态
     */
    public Map<String, Object> getReportedState(String deviceId) {
        return shadowRepository.findByDeviceId(deviceId)
                .map(shadow -> parseJsonToMap(shadow.getReportedState()))
                .orElse(Map.of());
    }

    /**
     * 删除设备影子
     */
    public void deleteShadow(String deviceId) {
        shadowRepository.deleteByDeviceId(deviceId);
        log.info("设备影子删除: {}", deviceId);
    }

    /**
     * 检查状态同步
     */
    public boolean isStateSynchronized(String deviceId) {
        return shadowRepository.findByDeviceId(deviceId)
                .map(shadow -> {
                    String desired = shadow.getDesiredState();
                    String reported = shadow.getReportedState();
                    return desired != null && reported != null && desired.equals(reported);
                })
                .orElse(false);
    }

    /**
     * 获取状态差异
     */
    public Map<String, Object> getStateDiff(String deviceId) {
        return shadowRepository.findByDeviceId(deviceId)
                .map(shadow -> {
                    Map<String, Object> desired = parseJsonToMap(shadow.getDesiredState());
                    Map<String, Object> reported = parseJsonToMap(shadow.getReportedState());
                    
                    return desired.entrySet().stream()
                            .filter(e -> !e.getValue().equals(reported.get(e.getKey())))
                            .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                })
                .orElse(java.util.Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败: {}", e.getMessage());
            return Map.of();
        }
    }
}