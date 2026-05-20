package com.smarthome.scene.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DeviceControlClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${device.service.url:http://localhost:8081}")
    private String deviceServiceUrl;

    public boolean sendCommand(String deviceId, String command) {
        try {
            String url = deviceServiceUrl + "/api/devices/" + deviceId + "/command";
            Map<String, String> body = new HashMap<>();
            body.put("command", command);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            log.info("已向设备服务下发命令: {} -> {}", deviceId, command);
            return true;
        } catch (Exception e) {
            log.error("设备命令下发失败: {} -> {}, {}", deviceId, command, e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listDevices() {
        try {
            String url = deviceServiceUrl + "/api/devices";
            List<Map<String, Object>> devices = restTemplate.getForObject(url, List.class);
            return devices != null ? devices : List.of();
        } catch (Exception e) {
            log.warn("获取设备列表失败: {}", e.getMessage());
            return List.of();
        }
    }
}
