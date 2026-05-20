package com.smarthome.device.service;

import com.smarthome.device.adapter.DeviceAdapter;
import com.smarthome.device.client.AnalyticsClient;
import com.smarthome.device.client.EdgeClient;
import com.smarthome.device.entity.Device;
import com.smarthome.device.entity.DeviceHeartbeat;
import com.smarthome.device.entity.DeviceWithStatus;
import com.smarthome.device.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备管理服务
 * 实现设备注册、鉴权、状态同步等核心功能
 */
@Slf4j
@Service
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final AnalyticsClient analyticsClient;
    private final EdgeClient edgeClient;
    private final Map<String, DeviceAdapter> deviceAdapters;
    
    @Autowired
    public DeviceService(DeviceRepository deviceRepository,
                        AnalyticsClient analyticsClient,
                        EdgeClient edgeClient,
                        List<DeviceAdapter> adapters) {
        this.deviceRepository = deviceRepository;
        this.analyticsClient = analyticsClient;
        this.edgeClient = edgeClient;
        this.deviceAdapters = new ConcurrentHashMap<>();
        
        // 注册所有设备适配器
        for (DeviceAdapter adapter : adapters) {
            deviceAdapters.put(adapter.getProtocol(), adapter);
        }
    }
    
    /**
     * 注册设备
     */
    public Device registerDevice(Device device) {
        // 如果没有设备ID，自动生成
        if (device.getDeviceId() == null || device.getDeviceId().isEmpty()) {
            device.setDeviceId(generateDeviceId());
        }
        
        log.info("注册设备: {}", device.getDeviceId());
        
        // 检查设备是否已存在
        if (deviceRepository.findByDeviceId(device.getDeviceId()).isPresent()) {
            throw new RuntimeException("设备已存在: " + device.getDeviceId());
        }
        
        // 检查MAC地址是否重复
        if (device.getMacAddress() != null && 
            deviceRepository.findByMacAddress(device.getMacAddress()).isPresent()) {
            throw new RuntimeException("MAC地址已存在: " + device.getMacAddress());
        }
        
        // 不设置默认状态，使用前端传入的状态
        device.setCreatedAt(LocalDateTime.now());
        
        Device savedDevice = deviceRepository.save(device);
        
        log.info("设备注册成功: {}", device.getDeviceId());
        return savedDevice;
    }
    
    /**
     * 生成设备ID
     */
    private String generateDeviceId() {
        return "device-" + System.currentTimeMillis() + "-" + 
               String.format("%04d", (int) (Math.random() * 10000));
    }
    
    /**
     * 获取设备信息
     */
    public Optional<Device> getDevice(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }
    
    /**
     * 获取所有设备
     */
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<DeviceWithStatus> getAllDevicesWithStatus() {
        List<Device> devices = deviceRepository.findAll();
        List<Map<String, Object>> statusList = edgeClient.getAllLatestDeviceStatuses();

        Map<String, Map<String, Object>> statusMap = new ConcurrentHashMap<>();
        if (statusList != null) {
            for (Map<String, Object> status : statusList) {
                String deviceId = (String) status.get("deviceId");
                if (deviceId != null) {
                    statusMap.put(deviceId, status);
                }
            }
        }

        List<DeviceWithStatus> result = new ArrayList<>();
        for (Device device : devices) {
            Map<String, Object> latestStatus = statusMap.get(device.getDeviceId());

            DeviceWithStatus dto = DeviceWithStatus.builder()
                    .id(device.getId())
                    .deviceId(device.getDeviceId())
                    .name(device.getName())
                    .type(device.getType())
                    .protocol(device.getProtocol())
                    .ipAddress(device.getIpAddress())
                    .macAddress(device.getMacAddress())
                    .firmwareVersion(device.getFirmwareVersion())
                    .manufacturer(device.getManufacturer())
                    .model(device.getModel())
                    .createdAt(device.getCreatedAt())
                    .updatedAt(device.getUpdatedAt())
                    .lastHeartbeat(device.getLastHeartbeat())
                    .build();

            if (latestStatus != null) {
                dto.setStatus((String) latestStatus.get("status"));
                dto.setPower(latestStatus.get("power") != null ? ((Number) latestStatus.get("power")).doubleValue() : null);
                dto.setTemperature(latestStatus.get("temperature") != null ? ((Number) latestStatus.get("temperature")).doubleValue() : null);
                dto.setHumidity(latestStatus.get("humidity") != null ? ((Number) latestStatus.get("humidity")).doubleValue() : null);
                dto.setProperties((String) latestStatus.get("properties"));
                Object updateTime = latestStatus.get("lastUpdateTime");
                if (updateTime != null) {
                    dto.setStatusUpdateTime(updateTime.toString());
                }
                if (dto.getStatus() == null) {
                    dto.setStatus(device.getStatus());
                }
            } else {
                dto.setStatus(device.getStatus());
            }

            result.add(dto);
        }

        return result;
    }
    
    /**
     * 删除设备
     */
    public void deleteDevice(Long id) {
        if (deviceRepository.existsById(id)) {
            deviceRepository.deleteById(id);
            log.info("设备已删除: {}", id);
        } else {
            throw new RuntimeException("设备不存在: " + id);
        }
    }
    
    /**
     * 根据deviceId删除设备
     */
    public void deleteDeviceByDeviceId(String deviceId) {
        Optional<Device> deviceOptional = deviceRepository.findByDeviceId(deviceId);
        if (deviceOptional.isPresent()) {
            deviceRepository.delete(deviceOptional.get());
            log.info("设备已删除: {}", deviceId);
        } else {
            throw new RuntimeException("设备不存在: " + deviceId);
        }
    }
    
    /**
     * 更新设备状态
     */
    public void updateDeviceStatus(String deviceId, String status) {
        Optional<Device> deviceOptional = deviceRepository.findByDeviceId(deviceId);
        deviceOptional.ifPresent(device -> {
            device.setStatus(status);
            device.setUpdatedAt(LocalDateTime.now());
            deviceRepository.save(device);
            
            log.info("设备状态更新: {} -> {}", deviceId, status);
        });
    }
    
    /**
     * 处理设备心跳
     */
    public void processHeartbeat(String deviceId, DeviceHeartbeat heartbeat) {
        // 更新设备最后心跳时间
        Optional<Device> deviceOptional = deviceRepository.findByDeviceId(deviceId);
        deviceOptional.ifPresent(device -> {
            device.setLastHeartbeat(LocalDateTime.now());
            device.setStatus("online");
            deviceRepository.save(device);
            
            // 保存心跳记录
            heartbeat.setDeviceId(deviceId);
            heartbeat.setTimestamp(LocalDateTime.now());
            // 这里应该保存到数据库，简化实现
        });
        
        log.debug("处理设备心跳: {}", deviceId);
    }
    
    /**
     * 发送设备命令
     */
    public boolean sendCommand(String deviceId, String command) {
        Optional<Device> deviceOptional = deviceRepository.findByDeviceId(deviceId);
        if (deviceOptional.isEmpty()) {
            log.error("设备不存在: {}", deviceId);
            return false;
        }
        
        Device device = deviceOptional.get();
        String protocol = device.getProtocol();
        
        if (protocol == null || protocol.isEmpty()) {
            protocol = "wifi";
        }
        
        DeviceAdapter adapter = deviceAdapters.get(protocol);
        
        applyStatusByCommand(device, command);
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(device);

        boolean success;
        if (adapter == null) {
            log.warn("不支持的设备协议: {}，使用默认处理", protocol);
            log.info("设备命令模拟执行成功: {} -> {}", deviceId, command);
            success = true;
        } else {
            success = adapter.sendCommand(device, command);
        }

        if (success) {
            analyticsClient.recordEnergy(device, command);
            analyticsClient.recordBehavior(device, command, "1", "user");
        }
        return success;
    }

    private void applyStatusByCommand(Device device, String command) {
        if (command == null) {
            return;
        }
        String normalized = command.toLowerCase();
        if ("on".equals(normalized) || "turn_on".equals(normalized)) {
            device.setStatus("online");
        } else if ("off".equals(normalized) || "turn_off".equals(normalized)) {
            device.setStatus("offline");
        } else if ("dim".equals(normalized) || "brighten".equals(normalized) || "settemp".equals(normalized)) {
            device.setStatus("online");
        }
    }
    
    /**
     * 检查设备在线状态
     */
    @Scheduled(fixedRate = 30000) // 每30秒执行一次
    public void checkDeviceOnlineStatus() {
        log.info("开始检查设备在线状态");
        
        LocalDateTime timeoutTime = LocalDateTime.now().minusSeconds(60);
        List<Device> expiredDevices = deviceRepository.findDevicesWithExpiredHeartbeat(timeoutTime);
        
        for (Device device : expiredDevices) {
            if ("online".equals(device.getStatus())) {
                device.setStatus("offline");
                deviceRepository.save(device);
                log.info("设备状态变更为离线: {}", device.getDeviceId());
            }
        }
    }
    
    /**
     * 获取设备统计信息
     */
    public Map<String, Long> getDeviceStatistics() {
        Map<String, Long> statistics = new ConcurrentHashMap<>();
        statistics.put("total", deviceRepository.count());
        statistics.put("online", deviceRepository.countOnlineDevices());
        statistics.put("offline", deviceRepository.countOfflineDevices());
        statistics.put("warning", deviceRepository.countWarningDevices());
        
        return statistics;
    }
}