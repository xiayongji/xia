package com.smarthome.edge;

import com.smarthome.edge.entity.CommandResult;
import com.smarthome.edge.entity.Device;
import com.smarthome.edge.entity.DeviceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdgeGatewayService {

    private final LocalDeviceManager localDeviceManager;
    private final CloudSyncService cloudSyncService;

    private final Map<String, Long> lastCommandTime = new ConcurrentHashMap<>();
    private static final long MIN_COMMAND_INTERVAL = 50;

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
    public void syncLocalStatusPeriodically() {
        Map<String, DeviceStatus> localStatusMap = localDeviceManager.getAllLocalDeviceStatus();
        if (!localStatusMap.isEmpty()) {
            cloudSyncService.batchSyncStatus(localStatusMap);
        }
    }

    public DeviceStatus getDeviceStatus(String deviceId) {
        if (localDeviceManager.isLocalDevice(deviceId)) {
            return localDeviceManager.getDeviceStatus(deviceId);
        }
        return cloudSyncService.getRemoteDeviceStatus(deviceId);
    }

    public List<String> getLocalDeviceIds() {
        return localDeviceManager.getLocalDeviceIds();
    }

    public boolean registerLocalDevice(Device device) {
        return localDeviceManager.registerDevice(device);
    }

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
}