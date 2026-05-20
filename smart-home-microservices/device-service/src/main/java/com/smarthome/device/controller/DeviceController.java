package com.smarthome.device.controller;

import com.smarthome.device.entity.Device;
import com.smarthome.device.entity.DeviceHeartbeat;
import com.smarthome.device.entity.DeviceShadow;
import com.smarthome.device.entity.DeviceWithStatus;
import com.smarthome.device.service.DeviceService;
import com.smarthome.device.service.DeviceShadowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 设备管理控制器
 * 提供设备注册、状态监控、命令下发等REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    
    private final DeviceService deviceService;
    private final DeviceShadowService shadowService;
    
    /**
     * 注册设备（兼容旧接口）
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerDevice(@RequestBody Device device) {
        try {
            Device registeredDevice = deviceService.registerDevice(device);
            return new ResponseEntity<>(registeredDevice, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("设备注册失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 添加设备（前端调用接口）
     */
    @PostMapping
    public ResponseEntity<?> addDevice(@RequestBody Map<String, String> deviceData) {
        try {
            Device device = new Device();
            device.setDeviceId(generateDeviceId());
            device.setName(deviceData.get("name"));
            device.setType(deviceData.get("type"));
            String protocol = deviceData.getOrDefault("protocol", "wifi");
            device.setProtocol(protocol != null ? protocol.toLowerCase() : "wifi");
            
            // 使用前端传入的状态，如果没有则默认为offline
            String status = deviceData.getOrDefault("status", "offline");
            device.setStatus(status);
            
            // 额外字段（可选）
            if (deviceData.containsKey("manufacturer")) {
                device.setManufacturer(deviceData.get("manufacturer"));
            }
            if (deviceData.containsKey("model")) {
                device.setModel(deviceData.get("model"));
            }
            
            Device registeredDevice = deviceService.registerDevice(device);
            return new ResponseEntity<>(registeredDevice, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("添加设备失败: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    private String generateDeviceId() {
        return "device-" + System.currentTimeMillis() + "-" + 
               String.format("%04d", (int) (Math.random() * 10000));
    }
    
    /**
     * 获取所有设备
     */
    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices();
        return new ResponseEntity<>(devices, HttpStatus.OK);
    }

    @GetMapping("/with-status")
    public ResponseEntity<List<DeviceWithStatus>> getAllDevicesWithStatus() {
        List<DeviceWithStatus> devices = deviceService.getAllDevicesWithStatus();
        return new ResponseEntity<>(devices, HttpStatus.OK);
    }
    
    /**
     * 获取设备详情
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<Device> getDevice(@PathVariable String deviceId) {
        Optional<Device> device = deviceService.getDevice(deviceId);
        return device.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 更新设备状态
     */
    @PutMapping("/{deviceId}/status")
    public ResponseEntity<?> updateDeviceStatus(@PathVariable String deviceId, 
                                               @RequestBody Map<String, String> statusData) {
        String status = statusData.get("status");
        if (status == null || status.isEmpty()) {
            return new ResponseEntity<>("状态不能为空", HttpStatus.BAD_REQUEST);
        }
        
        Optional<Device> deviceOpt = deviceService.getDevice(deviceId);
        if (deviceOpt.isEmpty()) {
            return new ResponseEntity<>("设备不存在", HttpStatus.NOT_FOUND);
        }
        
        deviceService.updateDeviceStatus(deviceId, status);
        Device updatedDevice = deviceService.getDevice(deviceId).get();
        return new ResponseEntity<>(updatedDevice, HttpStatus.OK);
    }
    
    /**
     * 处理设备心跳
     */
    @PostMapping("/{deviceId}/heartbeat")
    public ResponseEntity<Void> processHeartbeat(@PathVariable String deviceId,
                                                 @RequestBody DeviceHeartbeat heartbeat) {
        deviceService.processHeartbeat(deviceId, heartbeat);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    /**
     * 发送设备命令
     */
    @PostMapping("/{deviceId}/command")
    public ResponseEntity<?> sendCommand(@PathVariable String deviceId,
                                        @RequestBody Map<String, String> command) {
        String commandStr = command.get("command");
        if (commandStr == null) {
            return new ResponseEntity<>("命令不能为空", HttpStatus.BAD_REQUEST);
        }
        
        boolean success = deviceService.sendCommand(deviceId, commandStr);
        if (success) {
            return new ResponseEntity<>("命令发送成功", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("命令发送失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取设备统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getDeviceStatistics() {
        Map<String, Long> statistics = deviceService.getDeviceStatistics();
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }
    
    /**
     * 删除设备
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> deleteDevice(@PathVariable String deviceId) {
        try {
            deviceService.deleteDeviceByDeviceId(deviceId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("删除设备失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * 获取设备影子
     */
    @GetMapping("/{deviceId}/shadow")
    public ResponseEntity<DeviceShadow> getDeviceShadow(@PathVariable String deviceId) {
        Optional<DeviceShadow> shadow = shadowService.getShadow(deviceId);
        return shadow.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 更新设备期望状态
     */
    @PutMapping("/{deviceId}/shadow/desired")
    public ResponseEntity<Void> updateDesiredState(@PathVariable String deviceId,
                                                   @RequestBody Map<String, Object> desiredState) {
        shadowService.updateDesiredState(deviceId, desiredState);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    /**
     * 更新设备上报状态
     */
    @PutMapping("/{deviceId}/shadow/reported")
    public ResponseEntity<Void> updateReportedState(@PathVariable String deviceId,
                                                    @RequestBody Map<String, Object> reportedState) {
        shadowService.updateReportedState(deviceId, reportedState);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    /**
     * 检查状态同步
     */
    @GetMapping("/{deviceId}/shadow/sync")
    public ResponseEntity<Boolean> checkStateSync(@PathVariable String deviceId) {
        boolean synchronizedState = shadowService.isStateSynchronized(deviceId);
        return new ResponseEntity<>(synchronizedState, HttpStatus.OK);
    }
    
    /**
     * 获取状态差异
     */
    @GetMapping("/{deviceId}/shadow/diff")
    public ResponseEntity<Map<String, Object>> getStateDiff(@PathVariable String deviceId) {
        Map<String, Object> diff = shadowService.getStateDiff(deviceId);
        return new ResponseEntity<>(diff, HttpStatus.OK);
    }
}