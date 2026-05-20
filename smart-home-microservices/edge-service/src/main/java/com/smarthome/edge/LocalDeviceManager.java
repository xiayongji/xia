package com.smarthome.edge;

import com.smarthome.edge.entity.CommandResult;
import com.smarthome.edge.entity.Device;
import com.smarthome.edge.entity.DeviceStatus;
import com.smarthome.edge.repository.DeviceRepository;
import com.smarthome.edge.repository.DeviceStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalDeviceManager {

    private final DeviceRepository deviceRepository;
    private final DeviceStatusRepository deviceStatusRepository;

    @Transactional
    public boolean registerDevice(Device device) {
        try {
            if (deviceRepository.existsByDeviceId(device.getDeviceId())) {
                log.warn("设备已存在 - 设备ID: {}", device.getDeviceId());
                return false;
            }

            device.setStatus("online");
            deviceRepository.save(device);

            DeviceStatus status = DeviceStatus.builder()
                    .deviceId(device.getDeviceId())
                    .status("online")
                    .deviceType(device.getDeviceType())
                    .power(0.0)
                    .lastUpdateTime(LocalDateTime.now())
                    .build();
            deviceStatusRepository.save(status);

            log.info("本地设备注册成功 - 设备ID: {}, 协议: {}", device.getDeviceId(), device.getProtocol());
            return true;
        } catch (Exception e) {
            log.error("设备注册失败 - 设备ID: {}, 错误: {}", device.getDeviceId(), e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean unregisterDevice(String deviceId) {
        try {
            if (!deviceRepository.existsByDeviceId(deviceId)) {
                log.warn("设备不存在 - 设备ID: {}", deviceId);
                return false;
            }

            deviceRepository.deleteById(deviceId);
            deviceStatusRepository.deleteByDeviceId(deviceId);

            log.info("本地设备注销成功 - 设备ID: {}", deviceId);
            return true;
        } catch (Exception e) {
            log.error("设备注销失败 - 设备ID: {}, 错误: {}", deviceId, e.getMessage());
            return false;
        }
    }

    public boolean isLocalDevice(String deviceId) {
        return deviceRepository.existsByDeviceId(deviceId);
    }

    public Device getLocalDevice(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId).orElse(null);
    }

    @Transactional
    public CommandResult executeLocalCommand(Device device, String command) {
        String protocol = device.getProtocol();
        CommandResult result = switch (protocol.toLowerCase()) {
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

        updateDeviceStatus(device.getDeviceId(), "online");
        return result;
    }

    private CommandResult executeWiFiCommand(Device device, String command) {
        log.debug("执行WiFi命令 - 设备: {}, 命令: {}", device.getDeviceId(), command);

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

        return CommandResult.builder()
                .deviceId(device.getDeviceId())
                .success(true)
                .message("命令执行成功")
                .response("ZigBee命令执行完成: " + command)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public DeviceStatus getDeviceStatus(String deviceId) {
        return deviceStatusRepository.findByDeviceId(deviceId).orElse(null);
    }

    public DeviceStatus getLatestDeviceStatus(String deviceId) {
        return deviceStatusRepository.findLatestByDeviceId(deviceId).orElse(null);
    }

    public Map<String, DeviceStatus> getAllLocalDeviceStatus() {
        List<Device> devices = deviceRepository.findAll();
        List<String> deviceIds = devices.stream()
                .map(Device::getDeviceId)
                .collect(Collectors.toList());

        if (deviceIds.isEmpty()) {
            return Map.of();
        }

        List<DeviceStatus> statuses = deviceStatusRepository.findByDeviceIdInOrderByLastUpdateTimeDesc(deviceIds);

        return statuses.stream()
                .collect(Collectors.toMap(
                        DeviceStatus::getDeviceId,
                        status -> status,
                        (existing, replacement) -> replacement
                ));
    }

    public List<DeviceStatus> getAllDeviceStatusesFromDatabase() {
        return deviceStatusRepository.findAllOrderByLastUpdateTimeDesc();
    }

    public List<Device> getAllLocalDevices() {
        return deviceRepository.findAllOrderByUpdateTimeDesc();
    }

    public List<String> getLocalDeviceIds() {
        return deviceRepository.findAll().stream()
                .map(Device::getDeviceId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateDeviceStatus(String deviceId, String status) {
        try {
            DeviceStatus existingStatus = deviceStatusRepository.findByDeviceId(deviceId).orElse(null);

            if (existingStatus != null) {
                existingStatus.setStatus(status);
                existingStatus.setLastUpdateTime(LocalDateTime.now());
                deviceStatusRepository.save(existingStatus);
            } else {
                Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
                if (device != null) {
                    DeviceStatus newStatus = DeviceStatus.builder()
                            .deviceId(deviceId)
                            .status(status)
                            .deviceType(device.getDeviceType())
                            .power(0.0)
                            .lastUpdateTime(LocalDateTime.now())
                            .build();
                    deviceStatusRepository.save(newStatus);
                }
            }

            log.debug("设备状态更新成功 - 设备ID: {}, 状态: {}", deviceId, status);
        } catch (Exception e) {
            log.error("设备状态更新失败 - 设备ID: {}, 错误: {}", deviceId, e.getMessage());
        }
    }

    @Transactional
    public void updateDeviceStatusWithDetails(String deviceId, String status, Double power,
                                               Double temperature, Double humidity, String properties) {
        try {
            DeviceStatus existingStatus = deviceStatusRepository.findByDeviceId(deviceId).orElse(null);

            if (existingStatus != null) {
                existingStatus.setStatus(status);
                existingStatus.setPower(power);
                existingStatus.setTemperature(temperature);
                existingStatus.setHumidity(humidity);
                existingStatus.setProperties(properties);
                existingStatus.setLastUpdateTime(LocalDateTime.now());
                deviceStatusRepository.save(existingStatus);
            } else {
                Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
                if (device != null) {
                    DeviceStatus newStatus = DeviceStatus.builder()
                            .deviceId(deviceId)
                            .status(status)
                            .deviceType(device.getDeviceType())
                            .power(power != null ? power : 0.0)
                            .temperature(temperature)
                            .humidity(humidity)
                            .properties(properties)
                            .lastUpdateTime(LocalDateTime.now())
                            .build();
                    deviceStatusRepository.save(newStatus);
                }
            }

            log.debug("设备状态详情更新成功 - 设备ID: {}, 状态: {}, 功率: {}W", deviceId, status, power);
        } catch (Exception e) {
            log.error("设备状态详情更新失败 - 设备ID: {}, 错误: {}", deviceId, e.getMessage());
        }
    }

    public long countOnlineDevices() {
        return deviceStatusRepository.countOnlineDevices();
    }

    @Transactional
    public void batchUpdateDeviceStatus(Map<String, String> statusMap) {
        for (Map.Entry<String, String> entry : statusMap.entrySet()) {
            updateDeviceStatus(entry.getKey(), entry.getValue());
        }
    }
}
