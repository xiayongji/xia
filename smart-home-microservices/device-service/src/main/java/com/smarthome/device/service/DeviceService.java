package com.smarthome.device.service;

import com.smarthome.device.adapter.DeviceAdapter;
import com.smarthome.device.entity.Device;
import com.smarthome.device.entity.DeviceHeartbeat;
import com.smarthome.device.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final Map<String, DeviceAdapter> deviceAdapters;
    
    // 设备适配器映射
    @Autowired
    public DeviceService(DeviceRepository deviceRepository, 
                        List<DeviceAdapter> adapters) {
        this.deviceRepository = deviceRepository;
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
        
        // 设置默认状态
        device.setStatus("offline");
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
        DeviceAdapter adapter = deviceAdapters.get(device.getProtocol());
        
        if (adapter == null) {
            log.error("不支持的设备协议: {}", device.getProtocol());
            return false;
        }
        
        return adapter.sendCommand(device, command);
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