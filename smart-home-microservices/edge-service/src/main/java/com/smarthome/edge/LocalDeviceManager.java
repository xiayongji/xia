package com.smarthome.edge;

import com.smarthome.edge.entity.CommandResult;
import com.smarthome.edge.entity.Device;
import com.smarthome.edge.entity.DeviceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class LocalDeviceManager {

    private final Map<String, Device> localDevices = new ConcurrentHashMap<>();
    private final Map<String, DeviceStatus> deviceStatusMap = new ConcurrentHashMap<>();

    public boolean registerDevice(Device device) {
        localDevices.put(device.getDeviceId(), device);
        deviceStatusMap.put(device.getDeviceId(), DeviceStatus.builder()
                .deviceId(device.getDeviceId())
                .status("online")
                .deviceType(device.getDeviceType())
                .lastUpdateTime(LocalDateTime.now())
                .build());
        
        log.info("本地设备注册成功 - 设备ID: {}, 协议: {}", device.getDeviceId(), device.getProtocol());
        return true;
    }

    public boolean unregisterDevice(String deviceId) {
        localDevices.remove(deviceId);
        deviceStatusMap.remove(deviceId);
        log.info("本地设备注销成功 - 设备ID: {}", deviceId);
        return true;
    }

    public boolean isLocalDevice(String deviceId) {
        return localDevices.containsKey(deviceId);
    }

    public Device getLocalDevice(String deviceId) {
        return localDevices.get(deviceId);
    }

    public CommandResult executeLocalCommand(Device device, String command) {
        String protocol = device.getProtocol();
        return switch (protocol.toLowerCase()) {
            case "wifi" -> executeWiFiCommand(device, command);
            case "bluetooth" -> executeBluetoothCommand(device, command);
            case "zigbee" -> executeZigBeeCommand(device, command);
            default -> CommandResult.builder()
                    .deviceId(device.getDeviceId())
                    .success(false)
                    .message("不支持的协议: " + protocol)
                    .timestamp(LocalDateTime.now())
                    .build();
        };
    }

    private CommandResult executeWiFiCommand(Device device, String command) {
        log.debug("执行WiFi命令 - 设备: {}, 命令: {}", device.getDeviceId(), command);
        updateDeviceStatus(device.getDeviceId(), "online");
        return CommandResult.builder()
                .deviceId(device.getDeviceId())
                .success(true)
                .message("命令执行成功")
                .response("WiFi命令执行完成: " + command)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private CommandResult executeBluetoothCommand(Device device, String command) {
        log.debug("执行蓝牙命令 - 设备: {}, 命令: {}", device.getDeviceId(), command);
        updateDeviceStatus(device.getDeviceId(), "online");
        return CommandResult.builder()
                .deviceId(device.getDeviceId())
                .success(true)
                .message("命令执行成功")
                .response("蓝牙命令执行完成: " + command)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private CommandResult executeZigBeeCommand(Device device, String command) {
        log.debug("执行ZigBee命令 - 设备: {}, 命令: {}", device.getDeviceId(), command);
        updateDeviceStatus(device.getDeviceId(), "online");
        return CommandResult.builder()
                .deviceId(device.getDeviceId())
                .success(true)
                .message("命令执行成功")
                .response("ZigBee命令执行完成: " + command)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public DeviceStatus getDeviceStatus(String deviceId) {
        return deviceStatusMap.get(deviceId);
    }

    public Map<String, DeviceStatus> getAllLocalDeviceStatus() {
        return new ConcurrentHashMap<>(deviceStatusMap);
    }

    public List<String> getLocalDeviceIds() {
        return localDevices.keySet().stream().toList();
    }

    private void updateDeviceStatus(String deviceId, String status) {
        deviceStatusMap.computeIfPresent(deviceId, (k, v) -> DeviceStatus.builder()
                .deviceId(v.getDeviceId())
                .status(status)
                .deviceType(v.getDeviceType())
                .lastUpdateTime(LocalDateTime.now())
                .build());
    }
}