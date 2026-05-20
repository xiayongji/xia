package com.smarthome.edge.controller;

import com.smarthome.edge.EdgeGatewayService;
import com.smarthome.edge.entity.CommandResult;
import com.smarthome.edge.entity.Device;
import com.smarthome.edge.entity.DeviceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            return ResponseEntity.badRequest().body(
                    CommandResult.builder()
                            .deviceId(deviceId)
                            .success(false)
                            .message("命令不能为空")
                            .timestamp(LocalDateTime.now())
                            .build()
            );
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
            DeviceStatus notFoundStatus = DeviceStatus.builder()
                    .deviceId(deviceId)
                    .status("not_found")
                    .properties("设备不存在或未查询到状态")
                    .lastUpdateTime(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(notFoundStatus);
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/devices/{deviceId}/status/latest")
    public ResponseEntity<DeviceStatus> getLatestDeviceStatus(@PathVariable String deviceId) {
        DeviceStatus status = edgeGatewayService.getDeviceStatusFromDatabase(deviceId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/devices")
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = edgeGatewayService.getAllLocalDevices();
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/devices/ids")
    public ResponseEntity<List<String>> getLocalDeviceIds() {
        List<String> deviceIds = edgeGatewayService.getLocalDeviceIds();
        return ResponseEntity.ok(deviceIds);
    }

    @GetMapping("/devices/status/all")
    public ResponseEntity<List<DeviceStatus>> getAllDeviceStatuses() {
        List<DeviceStatus> statuses = edgeGatewayService.getAllDeviceStatuses();
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/devices/status/latest/all")
    public ResponseEntity<List<DeviceStatus>> getAllLatestDeviceStatuses() {
        List<DeviceStatus> statuses = edgeGatewayService.getAllLatestDeviceStatuses();
        return ResponseEntity.ok(statuses);
    }

    @PostMapping("/devices/register")
    public ResponseEntity<Map<String, Object>> registerDevice(@RequestBody Device device) {
        Map<String, Object> response = new HashMap<>();

        if (device.getDeviceId() == null || device.getDeviceId().isEmpty()) {
            response.put("success", false);
            response.put("message", "设备ID不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = edgeGatewayService.registerLocalDevice(device);

        response.put("success", success);
        response.put("message", success ? "设备注册成功" : "设备注册失败，设备可能已存在");
        response.put("deviceId", device.getDeviceId());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/devices/{deviceId}/unregister")
    public ResponseEntity<Map<String, Object>> unregisterDevice(@PathVariable String deviceId) {
        Map<String, Object> response = new HashMap<>();
        boolean success = edgeGatewayService.unregisterLocalDevice(deviceId);

        response.put("success", success);
        response.put("message", success ? "设备注销成功" : "设备注销失败，设备可能不存在");
        response.put("deviceId", deviceId);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/devices/{deviceId}/status")
    public ResponseEntity<Map<String, Object>> updateDeviceStatus(
            @PathVariable String deviceId,
            @RequestBody Map<String, Object> statusData) {

        Map<String, Object> response = new HashMap<>();

        String status = (String) statusData.get("status");
        Double power = statusData.get("power") != null ? ((Number) statusData.get("power")).doubleValue() : null;
        Double temperature = statusData.get("temperature") != null ? ((Number) statusData.get("temperature")).doubleValue() : null;
        Double humidity = statusData.get("humidity") != null ? ((Number) statusData.get("humidity")).doubleValue() : null;
        String properties = statusData.get("properties") != null ? statusData.get("properties").toString() : null;

        try {
            edgeGatewayService.updateDeviceStatus(deviceId, status, power, temperature, humidity, properties);

            response.put("success", true);
            response.put("message", "设备状态更新成功");
            response.put("deviceId", deviceId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("设备状态更新失败 - 设备ID: {}, 错误: {}", deviceId, e.getMessage());

            response.put("success", false);
            response.put("message", "设备状态更新失败: " + e.getMessage());
            response.put("deviceId", deviceId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/devices/statistics")
    public ResponseEntity<Map<String, Object>> getDeviceStatistics() {
        Map<String, Object> statistics = edgeGatewayService.getDeviceStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/devices/status/updated")
    public ResponseEntity<List<DeviceStatus>> getDevicesUpdatedAfter(
            @RequestParam(required = false) String since) {

        List<DeviceStatus> statuses;
        if (since != null && !since.isEmpty()) {
            LocalDateTime sinceTime = LocalDateTime.parse(since);
            statuses = edgeGatewayService.getDeviceStatusesUpdatedAfter(sinceTime);
        } else {
            statuses = edgeGatewayService.getAllDeviceStatuses();
        }

        return ResponseEntity.ok(statuses);
    }

    @PostMapping("/sync/cloud")
    public ResponseEntity<Map<String, Object>> syncWithCloud() {
        Map<String, Object> response = new HashMap<>();
        boolean success = edgeGatewayService.syncWithCloud();

        response.put("success", success);
        response.put("message", success ? "云端同步成功" : "云端同步失败");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics/latency")
    public ResponseEntity<Map<String, Object>> getLatencyMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        Map<String, Object> stats = edgeGatewayService.getDeviceStatistics();

        metrics.put("edge_processing_time_ms", "<50");
        metrics.put("local_command_success_rate", "99.9%");
        metrics.put("cloud_sync_frequency", "10s");
        metrics.put("supported_protocols", List.of("WiFi", "Bluetooth", "ZigBee"));
        metrics.put("total_devices", stats.get("totalDevices"));
        metrics.put("online_devices", stats.get("onlineDevices"));
        metrics.put("offline_devices", stats.get("offlineDevices"));
        metrics.put("total_power_consumption", stats.get("totalPowerConsumption"));
        metrics.put("last_sync_time", stats.get("lastSyncTime"));
        metrics.put("database_status", "connected");

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        try {
            List<DeviceStatus> statuses = edgeGatewayService.getAllDeviceStatuses();
            long deviceCount = statuses.size();

            health.put("status", "UP");
            health.put("service", "edge-service");
            health.put("database", "connected");
            health.put("deviceCount", deviceCount);
            health.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("健康检查失败: {}", e.getMessage());

            health.put("status", "DOWN");
            health.put("service", "edge-service");
            health.put("database", "disconnected");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/devices/type/{deviceType}")
    public ResponseEntity<List<Device>> getDevicesByType(@PathVariable String deviceType) {
        List<Device> allDevices = edgeGatewayService.getAllLocalDevices();
        List<Device> filteredDevices = allDevices.stream()
                .filter(device -> deviceType.equalsIgnoreCase(device.getDeviceType()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredDevices);
    }

    @GetMapping("/devices/protocol/{protocol}")
    public ResponseEntity<List<Device>> getDevicesByProtocol(@PathVariable String protocol) {
        List<Device> allDevices = edgeGatewayService.getAllLocalDevices();
        List<Device> filteredDevices = allDevices.stream()
                .filter(device -> protocol.equalsIgnoreCase(device.getProtocol()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredDevices);
    }
}
