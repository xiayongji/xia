package com.smarthome.scene.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.scene.client.DeviceControlClient;
import com.smarthome.scene.entity.Scene;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SceneDeviceActionService {

    private final DeviceControlClient deviceControlClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public int executeSceneActions(Scene scene) {
        if (scene == null || scene.getActions() == null || scene.getActions().isBlank()) {
            return executePresetByName(scene);
        }

        List<Map<String, Object>> actions = parseActions(scene.getActions());
        if (actions.isEmpty()) {
            return executePresetByName(scene);
        }

        int executed = 0;
        for (Map<String, Object> action : actions) {
            String deviceId = (String) action.get("deviceId");
            String command = (String) action.getOrDefault("action", action.get("command"));
            if (deviceId == null || command == null) {
                continue;
            }
            if (deviceControlClient.sendCommand(deviceId, command)) {
                executed++;
            }
        }
        return executed;
    }

    private int executePresetByName(Scene scene) {
        if (scene == null || scene.getName() == null) {
            return 0;
        }
        String name = scene.getName();
        List<Map<String, Object>> devices = deviceControlClient.listDevices();
        if (devices.isEmpty()) {
            return 0;
        }

        int executed = 0;
        if (name.contains("回家")) {
            executed += controlByType(devices, List.of("light", "灯", "ac", "空调"), "on");
        } else if (name.contains("离家")) {
            executed += controlByType(devices, List.of("light", "灯", "ac", "空调", "tv", "电视"), "off");
        } else if (name.contains("睡眠")) {
            executed += controlByType(devices, List.of("light", "灯", "tv", "电视"), "off");
        } else if (name.contains("阅读")) {
            executed += controlByType(devices, List.of("light", "灯"), "on");
        }
        return executed;
    }

    private int controlByType(List<Map<String, Object>> devices, List<String> keywords, String command) {
        int count = 0;
        for (Map<String, Object> device : devices) {
            String deviceId = (String) device.get("deviceId");
            if (deviceId == null) {
                deviceId = (String) device.get("id");
            }
            String deviceName = String.valueOf(device.getOrDefault("name", ""));
            String deviceType = String.valueOf(device.getOrDefault("type", ""));
            String haystack = (deviceName + " " + deviceType).toLowerCase();
            boolean match = keywords.stream().anyMatch(k -> haystack.contains(k.toLowerCase()));
            if (match && deviceControlClient.sendCommand(deviceId, command)) {
                count++;
            }
        }
        return count;
    }

    private List<Map<String, Object>> parseActions(String actionsJson) {
        try {
            return objectMapper.readValue(actionsJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.warn("解析场景动作 JSON 失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
