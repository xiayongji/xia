package com.smarthome.device.client;

import com.smarthome.device.entity.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AnalyticsClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${analytics.service.url:http://localhost:8085}")
    private String analyticsServiceUrl;

    @Async
    public void recordEnergy(Device device, String command) {
        if (device == null) {
            return;
        }
        try {
            double power = estimatePower(device, command);
            double energy = power * 0.05;

            Map<String, Object> body = new HashMap<>();
            body.put("deviceId", device.getDeviceId());
            body.put("deviceName", device.getName());
            body.put("deviceType", device.getType());
            body.put("power", power);
            body.put("energy", energy);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = analyticsServiceUrl + "/api/analytics/energy";
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Object.class);
        } catch (Exception e) {
            log.debug("能耗记录失败: {}", e.getMessage());
        }
    }

    @Async
    public void recordBehavior(Device device, String command, String userId, String username) {
        if (device == null) {
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId != null ? userId : "1");
            body.put("username", username != null ? username : "user");
            body.put("behaviorType", "DEVICE_CONTROL");
            body.put("deviceId", device.getDeviceId());
            body.put("deviceName", device.getName());
            body.put("action", command);
            body.put("category", device.getType());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = analyticsServiceUrl + "/api/analytics/behavior";
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Object.class);
        } catch (Exception e) {
            log.debug("行为记录失败: {}", e.getMessage());
        }
    }

    private double estimatePower(Device device, String command) {
        String type = device.getType() != null ? device.getType().toLowerCase() : "";
        if ("off".equalsIgnoreCase(command) || "turn_off".equalsIgnoreCase(command)) {
            return 0.0;
        }
        if (type.contains("ac") || type.contains("空调")) {
            return 1500.0;
        }
        if (type.contains("light") || type.contains("灯")) {
            return 60.0;
        }
        if (type.contains("tv") || type.contains("电视")) {
            return 120.0;
        }
        return 100.0;
    }
}
