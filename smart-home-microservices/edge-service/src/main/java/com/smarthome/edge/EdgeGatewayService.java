package com.smarthome.edge;

import com.smarthome.edge.entity.CommandResult;
import com.smarthome.edge.entity.Device;
import com.smarthome.edge.entity.DeviceStatus;
import com.smarthome.edge.repository.DeviceRepository;
import com.smarthome.edge.repository.DeviceStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdgeGatewayService {

    private final LocalDeviceManager localDeviceManager;
    private final CloudSyncService cloudSyncService;
    private final DeviceRepository deviceRepository;
    private final DeviceStatusRepository deviceStatusRepository;

    private final Map<String, Long> lastCommandTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long MIN_COMMAND_INTERVAL = 50;

    @Transactional
    @CacheEvict(value = {"deviceStatus", "deviceList"}, key = "#deviceId")
    public CommandResult handleControlCommand(String deviceId, String command) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastCommandTime.get(deviceId);

        if (lastTime != null && currentTime - lastTime < MIN_COMMAND_INTERVAL) {
            return CommandResult.builder()
                    .deviceId(deviceId)
                    .success(false)
                    .message("操作过于频繁，请稍后重试")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        lastCommandTime.put(deviceId, currentTime);

        try {
            if (localDeviceManager.isLocalDevice(deviceId)) {
                long startTime = System.nanoTime();
                Device device = localDeviceManager.getLocalDevice(deviceId);

                if (device == null) {
                    return CommandResult.builder()
                            .deviceId(deviceId)
                            .success(false)
                            .message("设备不存在")
                            .timestamp(LocalDateTime.now())
                            .build();
                }

                CommandResult result = localDeviceManager.executeLocalCommand(device, command);
                long responseTime = (System.nanoTime() - startTime) / 1_000_000;

                log.info("边缘设备控制完成 - 设备ID: {}, 响应时间: {}ms", deviceId, responseTime);

                syncToCloudAsync(deviceId, command, result);

                return result;
            } else {
                return cloudSyncService.forwardToCloud(deviceId, command);
            }
        } catch (Exception e) {
            log.error("设备控制失败 - 设备ID: {}, 错误: {}", deviceId, e.getMessage());
            return CommandResult.builder()
                    .deviceId(deviceId)
                    .success(false)
                    .message("设备控制失败: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Async("edgeSyncExecutor")
    public CompletableFuture<Void> syncToCloudAsync(String deviceId, String command, CommandResult result) {
        cloudSyncService.syncCommandResult(deviceId, command, result);
        return CompletableFuture.completedFuture(null);
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void syncLocalStatusPeriodically() {
        Map<String, DeviceStatus> localStatusMap = localDeviceManager.getAllLocalDeviceStatus();
        if (!localStatusMap.isEmpty()) {
            cloudSyncService.batchSyncStatus(localStatusMap);
            log.debug("定期同步设备状态完成 - 设备数量: {}", localStatusMap.size());
        }
    }

    @Cacheable(value = "deviceStatus", key = "#deviceId")
    public DeviceStatus getDeviceStatus(String deviceId) {
        if (localDeviceManager.isLocalDevice(deviceId)) {
            return localDeviceManager.getLatestDeviceStatus(deviceId);
        }
        return cloudSyncService.getRemoteDeviceStatus(deviceId);
    }

    @Cacheable(value = "deviceStatus", key = "#deviceId")
    public DeviceStatus getDeviceStatusFromDatabase(String deviceId) {
        return deviceStatusRepository.findLatestByDeviceId(deviceId).orElse(null);
    }

    @Cacheable(value = "deviceList")
    public List<DeviceStatus> getAllDeviceStatuses() {
        return deviceStatusRepository.findAllOrderByLastUpdateTimeDesc();
    }

    @Cacheable(value = "deviceList")
    public List<Device> getAllLocalDevices() {
        return localDeviceManager.getAllLocalDevices();
    }

    public List<String> getLocalDeviceIds() {
        return localDeviceManager.getLocalDeviceIds();
    }

    @CacheEvict(value = {"deviceStatus", "deviceList"}, allEntries = true)
    public boolean registerLocalDevice(Device device) {
        return localDeviceManager.registerDevice(device);
    }

    @CacheEvict(value = {"deviceStatus", "deviceList"}, allEntries = true)
    public boolean unregisterLocalDevice(String deviceId) {
        return localDeviceManager.unregisterDevice(deviceId);
    }

    public boolean syncWithCloud() {
        try {
            syncLocalStatusPeriodically();
            return true;
        } catch (Exception e) {
            log.error("云同步失败: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getDeviceStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();

        long totalDevices = deviceRepository.count();
        long onlineDevices = localDeviceManager.countOnlineDevices();
        long offlineDevices = totalDevices - onlineDevices;

        List<DeviceStatus> allStatuses = deviceStatusRepository.findAllOrderByLastUpdateTimeDesc();

        double totalPower = allStatuses.stream()
                .filter(s -> s.getPower() != null)
                .mapToDouble(DeviceStatus::getPower)
                .sum();

        stats.put("totalDevices", totalDevices);
        stats.put("onlineDevices", onlineDevices);
        stats.put("offlineDevices", offlineDevices);
        stats.put("totalPowerConsumption", totalPower);
        stats.put("lastSyncTime", LocalDateTime.now());

        return stats;
    }

    @Transactional
    public void updateDeviceStatus(String deviceId, String status, Double power,
                                  Double temperature, Double humidity, String properties) {
        localDeviceManager.updateDeviceStatusWithDetails(deviceId, status, power, temperature, humidity, properties);
    }

    public List<DeviceStatus> getDeviceStatusesUpdatedAfter(LocalDateTime since) {
        return deviceStatusRepository.findUpdatedAfter(since);
    }

    public List<DeviceStatus> getAllLatestDeviceStatuses() {
        return deviceStatusRepository.findLatestStatusForAllDevices();
    }
}
