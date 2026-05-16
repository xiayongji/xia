package com.smarthome.edge.controller;

import com.smarthome.edge.EdgeGatewayService;
import com.smarthome.edge.entity.CommandResult;
import com.smarthome.edge.entity.Device;
import com.smarthome.edge.entity.DeviceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/edge")
@RequiredArgsConstructor
public class EdgeGatewayController {

    private final EdgeGatewayService edgeGatewayService;

    @PostMapping("/devices/{deviceId}/command")
    public ResponseEntity<CommandResult> controlDevice(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        
        String command = request.get("command");
        if (command == null || command.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        long startTime = System.nanoTime();
        CommandResult result = edgeGatewayService.handleControlCommand(deviceId, command);
        long responseTime = (System.nanoTime() - startTime) / 1_000_000;
        
        log.info("设备控制请求 - 设备ID: {}, 命令: {}, 响应时间: {}ms", deviceId, command, responseTime);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/devices/{deviceId}/status")
    public ResponseEntity<DeviceStatus> getDeviceStatus(@PathVariable String deviceId) {
        DeviceStatus status = edgeGatewayService.getDeviceStatus(deviceId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/devices")
    public ResponseEntity<List<String>> getLocalDevices() {
        List<String> deviceIds = edgeGatewayService.getLocalDeviceIds();
        return ResponseEntity.ok(deviceIds);
    }

    @PostMapping("/devices/register")
    public ResponseEntity<Boolean> registerDevice(@RequestBody Device device) {
        boolean success = edgeGatewayService.registerLocalDevice(device);
        return ResponseEntity.ok(success);
    }

    @DeleteMapping("/devices/{deviceId}/unregister")
    public ResponseEntity<Boolean> unregisterDevice(@PathVariable String deviceId) {
        boolean success = edgeGatewayService.unregisterLocalDevice(deviceId);
        return ResponseEntity.ok(success);
    }

    @GetMapping("/metrics/latency")
    public ResponseEntity<Map<String, Object>> getLatencyMetrics() {
        return ResponseEntity.ok(Map.of(
                "edge_processing_time_ms", "<50",
                "local_command_success_rate", "99.9%",
                "cloud_sync_frequency", "10s",
                "supported_protocols", List.of("WiFi", "Bluetooth", "ZigBee")
        ));
    }
}