package com.smarthome.device.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EdgeClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${edge.service.url:http://localhost:8084}")
    private String edgeServiceUrl;

    public List<Map<String, Object>> getAllLatestDeviceStatuses() {
        try {
            String url = edgeServiceUrl + "/api/edge/devices/status/latest/all";
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.warn("获取设备最新状态失败: {}", e.getMessage());
            return List.of();
        }
    }
}