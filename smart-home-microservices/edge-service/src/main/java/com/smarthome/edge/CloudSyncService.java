package com.smarthome.edge;

import com.smarthome.edge.entity.CommandResult;
import com.smarthome.edge.entity.DeviceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudSyncService {

    private final RestTemplate restTemplate;
    
    private static final String CLOUD_BASE_URL = "http://localhost:8080/api/cloud";

    public CommandResult forwardToCloud(String deviceId, String command) {
        try {
            String url = CLOUD_BASE_URL + "/devices/" + deviceId + "/command";
            
            CloudCommandRequest request = CloudCommandRequest.builder()
                    .deviceId(deviceId)
                    .command(command)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
            
            CommandResult result = restTemplate.postForObject(url, request, CommandResult.class);
            
            if (result != null) {
                log.info("云端命令执行完成 - 设备ID: {}, 结果: {}", deviceId, result.isSuccess());
                return result;
            }
            
            return CommandResult.builder()
                    .deviceId(deviceId)
                    .success(false)
                    .message("云端响应为空")
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("云端命令转发失败 - 设备ID: {}, 错误: {}", deviceId, e.getMessage());
            return CommandResult.builder()
                    .deviceId(deviceId)
                    .success(false)
                    .message("云端通信失败: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    public DeviceStatus getRemoteDeviceStatus(String deviceId) {
        try {
            String url = CLOUD_BASE_URL + "/devices/" + deviceId + "/status";
            return restTemplate.getForObject(url, DeviceStatus.class);
        } catch (Exception e) {
            log.error("获取远程设备状态失败 - 设备ID: {}, 错误: {}", deviceId, e.getMessage());
            return null;
        }
    }

    @Async("cloudSyncExecutor")
    public void syncCommandResult(String deviceId, String command, CommandResult result) {
        try {
            String url = CLOUD_BASE_URL + "/sync/command-result";
            
            CommandSyncRequest request = CommandSyncRequest.builder()
                    .deviceId(deviceId)
                    .command(command)
                    .success(result.isSuccess())
                    .message(result.getMessage())
                    .timestamp(result.getTimestamp().toString())
                    .build();
            
            restTemplate.postForObject(url, request, Void.class);
            
            log.debug("命令结果同步完成 - 设备ID: {}", deviceId);
        } catch (Exception e) {
            log.warn("命令结果同步失败 - 设备ID: {}, 错误: {}", deviceId, e.getMessage());
        }
    }

    @Async("cloudSyncExecutor")
    public void batchSyncStatus(Map<String, DeviceStatus> statusMap) {
        try {
            String url = CLOUD_BASE_URL + "/sync/status-batch";
            
            StatusBatchSyncRequest request = StatusBatchSyncRequest.builder()
                    .statusMap(statusMap)
                    .syncTime(LocalDateTime.now().toString())
                    .build();
            
            restTemplate.postForObject(url, request, Void.class);
            
            log.debug("批量状态同步完成 - 设备数量: {}", statusMap.size());
        } catch (Exception e) {
            log.warn("批量状态同步失败 - 错误: {}", e.getMessage());
        }
    }

    public List<DeviceStatus> fetchCloudDeviceStatus(List<String> deviceIds) {
        try {
            String url = CLOUD_BASE_URL + "/devices/status?ids=" + String.join(",", deviceIds);
            return restTemplate.getForObject(url, List.class);
        } catch (Exception e) {
            log.error("获取云端设备状态失败 - 错误: {}", e.getMessage());
            return List.of();
        }
    }
}