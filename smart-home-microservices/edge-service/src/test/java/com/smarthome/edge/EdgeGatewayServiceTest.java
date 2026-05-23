package com.smarthome.edge;

import com.smarthome.edge.entity.CommandResult;
import com.smarthome.edge.entity.Device;
import com.smarthome.edge.entity.DeviceStatus;
import com.smarthome.edge.repository.DeviceRepository;
import com.smarthome.edge.repository.DeviceStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdgeGatewayServiceTest {

    @Mock
    private LocalDeviceManager localDeviceManager;

    @Mock
    private CloudSyncService cloudSyncService;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceStatusRepository deviceStatusRepository;

    @InjectMocks
    private EdgeGatewayService edgeGatewayService;

    private static final String DEVICE_ID = "device-001";
    private static final String COMMAND = "turn_on";

    @BeforeEach
    void setUp() {
    }

    @Test
    void testHandleControlCommand_LocalDevice() {
        Device device = Device.builder()
                .deviceId(DEVICE_ID)
                .deviceType("light")
                .protocol("wifi")
                .status("online")
                .build();

        CommandResult expectedResult = CommandResult.builder()
                .deviceId(DEVICE_ID)
                .success(true)
                .message("命令执行成功")
                .timestamp(LocalDateTime.now())
                .build();

        when(localDeviceManager.isLocalDevice(DEVICE_ID)).thenReturn(true);
        when(localDeviceManager.getLocalDevice(DEVICE_ID)).thenReturn(device);
        when(localDeviceManager.executeLocalCommand(device, COMMAND)).thenReturn(expectedResult);

        CommandResult result = edgeGatewayService.handleControlCommand(DEVICE_ID, COMMAND);

        assertNotNull(result);
        assertEquals(DEVICE_ID, result.getDeviceId());
        assertTrue(result.isSuccess());
        assertEquals("命令执行成功", result.getMessage());

        verify(localDeviceManager).isLocalDevice(DEVICE_ID);
        verify(localDeviceManager).getLocalDevice(DEVICE_ID);
        verify(localDeviceManager).executeLocalCommand(device, COMMAND);
        verify(cloudSyncService).syncCommandResult(eq(DEVICE_ID), eq(COMMAND), any(CommandResult.class));
    }

    @Test
    void testHandleControlCommand_RemoteDevice() {
        CommandResult cloudResult = CommandResult.builder()
                .deviceId(DEVICE_ID)
                .success(true)
                .message("云端命令执行成功")
                .timestamp(LocalDateTime.now())
                .build();

        when(localDeviceManager.isLocalDevice(DEVICE_ID)).thenReturn(false);
        when(cloudSyncService.forwardToCloud(DEVICE_ID, COMMAND)).thenReturn(cloudResult);

        CommandResult result = edgeGatewayService.handleControlCommand(DEVICE_ID, COMMAND);

        assertNotNull(result);
        assertEquals(DEVICE_ID, result.getDeviceId());
        assertTrue(result.isSuccess());
        assertEquals("云端命令执行成功", result.getMessage());

        verify(localDeviceManager).isLocalDevice(DEVICE_ID);
        verify(cloudSyncService).forwardToCloud(DEVICE_ID, COMMAND);
        verify(localDeviceManager, never()).getLocalDevice(anyString());
    }

    @Test
    void testHandleControlCommand_TooFrequent() {
        Device device = Device.builder()
                .deviceId(DEVICE_ID)
                .deviceType("light")
                .protocol("wifi")
                .build();

        CommandResult successResult = CommandResult.builder()
                .deviceId(DEVICE_ID)
                .success(true)
                .message("命令执行成功")
                .timestamp(LocalDateTime.now())
                .build();

        when(localDeviceManager.isLocalDevice(DEVICE_ID)).thenReturn(true);
        when(localDeviceManager.getLocalDevice(DEVICE_ID)).thenReturn(device);
        when(localDeviceManager.executeLocalCommand(device, COMMAND)).thenReturn(successResult);

        edgeGatewayService.handleControlCommand(DEVICE_ID, COMMAND);

        CommandResult secondResult = edgeGatewayService.handleControlCommand(DEVICE_ID, COMMAND);

        assertNotNull(secondResult);
        assertEquals(DEVICE_ID, secondResult.getDeviceId());
        assertFalse(secondResult.isSuccess());
        assertTrue(secondResult.getMessage().contains("频繁"),
                "Message should contain frequency warning but was: " + secondResult.getMessage());
    }

    @Test
    void testHandleControlCommand_DeviceNotFound() {
        when(localDeviceManager.isLocalDevice(DEVICE_ID)).thenReturn(true);
        when(localDeviceManager.getLocalDevice(DEVICE_ID)).thenReturn(null);

        CommandResult result = edgeGatewayService.handleControlCommand(DEVICE_ID, COMMAND);

        assertNotNull(result);
        assertEquals(DEVICE_ID, result.getDeviceId());
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("设备不存在"),
                "Message should contain device not found but was: " + result.getMessage());

        verify(localDeviceManager).isLocalDevice(DEVICE_ID);
        verify(localDeviceManager).getLocalDevice(DEVICE_ID);
        verify(localDeviceManager, never()).executeLocalCommand(any(), anyString());
    }

    @Test
    void testHandleControlCommand_Exception() {
        Device device = Device.builder()
                .deviceId(DEVICE_ID)
                .deviceType("light")
                .protocol("wifi")
                .build();

        when(localDeviceManager.isLocalDevice(DEVICE_ID)).thenReturn(true);
        when(localDeviceManager.getLocalDevice(DEVICE_ID)).thenReturn(device);
        when(localDeviceManager.executeLocalCommand(device, COMMAND))
                .thenThrow(new RuntimeException("硬件通信异常"));

        CommandResult result = edgeGatewayService.handleControlCommand(DEVICE_ID, COMMAND);

        assertNotNull(result);
        assertEquals(DEVICE_ID, result.getDeviceId());
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("设备控制失败"),
                "Message should contain control failure but was: " + result.getMessage());
        assertTrue(result.getMessage().contains("硬件通信异常"),
                "Message should contain exception message but was: " + result.getMessage());
    }

    @Test
    void testGetDeviceStatus_Local() {
        DeviceStatus expectedStatus = DeviceStatus.builder()
                .deviceId(DEVICE_ID)
                .status("online")
                .power(100.0)
                .lastUpdateTime(LocalDateTime.now())
                .build();

        when(localDeviceManager.isLocalDevice(DEVICE_ID)).thenReturn(true);
        when(localDeviceManager.getLatestDeviceStatus(DEVICE_ID)).thenReturn(expectedStatus);

        DeviceStatus result = edgeGatewayService.getDeviceStatus(DEVICE_ID);

        assertNotNull(result);
        assertEquals(DEVICE_ID, result.getDeviceId());
        assertEquals("online", result.getStatus());
        assertEquals(100.0, result.getPower());

        verify(localDeviceManager).isLocalDevice(DEVICE_ID);
        verify(localDeviceManager).getLatestDeviceStatus(DEVICE_ID);
    }

    @Test
    void testGetDeviceStatus_Remote() {
        DeviceStatus expectedStatus = DeviceStatus.builder()
                .deviceId(DEVICE_ID)
                .status("online")
                .power(50.0)
                .lastUpdateTime(LocalDateTime.now())
                .build();

        when(localDeviceManager.isLocalDevice(DEVICE_ID)).thenReturn(false);
        when(cloudSyncService.getRemoteDeviceStatus(DEVICE_ID)).thenReturn(expectedStatus);

        DeviceStatus result = edgeGatewayService.getDeviceStatus(DEVICE_ID);

        assertNotNull(result);
        assertEquals(DEVICE_ID, result.getDeviceId());
        assertEquals("online", result.getStatus());
        assertEquals(50.0, result.getPower());

        verify(localDeviceManager).isLocalDevice(DEVICE_ID);
        verify(cloudSyncService).getRemoteDeviceStatus(DEVICE_ID);
    }

    @Test
    void testGetDeviceStatusFromDatabase() {
        DeviceStatus expectedStatus = DeviceStatus.builder()
                .deviceId(DEVICE_ID)
                .status("offline")
                .lastUpdateTime(LocalDateTime.now())
                .build();

        when(deviceStatusRepository.findLatestByDeviceId(DEVICE_ID))
                .thenReturn(java.util.Optional.of(expectedStatus));

        DeviceStatus result = edgeGatewayService.getDeviceStatusFromDatabase(DEVICE_ID);

        assertNotNull(result);
        assertEquals(DEVICE_ID, result.getDeviceId());
        assertEquals("offline", result.getStatus());

        verify(deviceStatusRepository).findLatestByDeviceId(DEVICE_ID);
    }

    @Test
    void testGetAllDeviceStatuses() {
        List<DeviceStatus> expectedStatuses = Arrays.asList(
                DeviceStatus.builder().deviceId("device-001").status("online").lastUpdateTime(LocalDateTime.now()).build(),
                DeviceStatus.builder().deviceId("device-002").status("offline").lastUpdateTime(LocalDateTime.now()).build()
        );

        when(deviceStatusRepository.findAllOrderByLastUpdateTimeDesc()).thenReturn(expectedStatuses);

        List<DeviceStatus> result = edgeGatewayService.getAllDeviceStatuses();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("device-001", result.get(0).getDeviceId());
        assertEquals("device-002", result.get(1).getDeviceId());

        verify(deviceStatusRepository).findAllOrderByLastUpdateTimeDesc();
    }

    @Test
    void testGetAllLocalDevices() {
        List<Device> expectedDevices = Arrays.asList(
                Device.builder().deviceId("device-001").deviceType("light").protocol("wifi").build(),
                Device.builder().deviceId("device-002").deviceType("sensor").protocol("zigbee").build()
        );

        when(localDeviceManager.getAllLocalDevices()).thenReturn(expectedDevices);

        List<Device> result = edgeGatewayService.getAllLocalDevices();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("device-001", result.get(0).getDeviceId());
        assertEquals("device-002", result.get(1).getDeviceId());

        verify(localDeviceManager).getAllLocalDevices();
    }

    @Test
    void testGetLocalDeviceIds() {
        List<String> expectedIds = Arrays.asList("device-001", "device-002", "device-003");

        when(localDeviceManager.getLocalDeviceIds()).thenReturn(expectedIds);

        List<String> result = edgeGatewayService.getLocalDeviceIds();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("device-001"));
        assertTrue(result.contains("device-002"));
        assertTrue(result.contains("device-003"));

        verify(localDeviceManager).getLocalDeviceIds();
    }

    @Test
    void testRegisterLocalDevice() {
        Device device = Device.builder()
                .deviceId(DEVICE_ID)
                .deviceType("light")
                .protocol("wifi")
                .build();

        when(localDeviceManager.registerDevice(device)).thenReturn(true);

        boolean result = edgeGatewayService.registerLocalDevice(device);

        assertTrue(result);
        verify(localDeviceManager).registerDevice(device);
    }

    @Test
    void testUnregisterLocalDevice() {
        when(localDeviceManager.unregisterDevice(DEVICE_ID)).thenReturn(true);

        boolean result = edgeGatewayService.unregisterLocalDevice(DEVICE_ID);

        assertTrue(result);
        verify(localDeviceManager).unregisterDevice(DEVICE_ID);
    }

    @Test
    void testSyncWithCloud_Success() {
        when(localDeviceManager.getAllLocalDeviceStatus()).thenReturn(Map.of());

        boolean result = edgeGatewayService.syncWithCloud();

        assertTrue(result);
    }

    @Test
    void testSyncWithCloud_Failure() {
        when(localDeviceManager.getAllLocalDeviceStatus())
                .thenThrow(new RuntimeException("同步服务不可用"));

        boolean result = edgeGatewayService.syncWithCloud();

        assertFalse(result);
    }

    @Test
    void testGetDeviceStatistics() {
        when(deviceRepository.count()).thenReturn(10L);
        when(localDeviceManager.countOnlineDevices()).thenReturn(5L);

        List<DeviceStatus> statuses = Arrays.asList(
                DeviceStatus.builder().deviceId("device-001").power(100.0).lastUpdateTime(LocalDateTime.now()).build(),
                DeviceStatus.builder().deviceId("device-002").power(200.0).lastUpdateTime(LocalDateTime.now()).build(),
                DeviceStatus.builder().deviceId("device-003").power(null).lastUpdateTime(LocalDateTime.now()).build()
        );
        when(deviceStatusRepository.findAllOrderByLastUpdateTimeDesc()).thenReturn(statuses);

        Map<String, Object> stats = edgeGatewayService.getDeviceStatistics();

        assertNotNull(stats);
        assertEquals(10L, stats.get("totalDevices"));
        assertEquals(5L, stats.get("onlineDevices"));
        assertEquals(5L, stats.get("offlineDevices"));
        assertEquals(300.0, stats.get("totalPowerConsumption"));
        assertNotNull(stats.get("lastSyncTime"));

        verify(deviceRepository).count();
        verify(localDeviceManager).countOnlineDevices();
        verify(deviceStatusRepository).findAllOrderByLastUpdateTimeDesc();
    }

    @Test
    void testUpdateDeviceStatus() {
        edgeGatewayService.updateDeviceStatus(DEVICE_ID, "online", 150.0, 25.5, 60.0, "{\"brightness\":80}");

        verify(localDeviceManager).updateDeviceStatusWithDetails(
                DEVICE_ID, "online", 150.0, 25.5, 60.0, "{\"brightness\":80}");
    }
}