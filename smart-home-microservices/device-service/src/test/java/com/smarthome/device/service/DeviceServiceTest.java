package com.smarthome.device.service;

import com.smarthome.device.adapter.DeviceAdapter;
import com.smarthome.device.client.AnalyticsClient;
import com.smarthome.device.client.EdgeClient;
import com.smarthome.device.entity.Device;
import com.smarthome.device.entity.DeviceHeartbeat;
import com.smarthome.device.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private AnalyticsClient analyticsClient;

    @Mock
    private EdgeClient edgeClient;

    @Mock
    private DeviceAdapter wifiAdapter;

    private DeviceService deviceService;

    private Device device;

    @BeforeEach
    void setUp() {
        when(wifiAdapter.getProtocol()).thenReturn("wifi");
        deviceService = new DeviceService(deviceRepository, analyticsClient, edgeClient, List.of(wifiAdapter));

        device = new Device();
        device.setId(1L);
        device.setName("测试设备");
        device.setType("light");
        device.setProtocol("wifi");
        device.setStatus("offline");
        device.setMacAddress("AA:BB:CC:DD:EE:FF");
        device.setManufacturer("测试厂商");
        device.setModel("TEST-001");
    }

    @Test
    void testRegisterDevice_Success() {
        Device inputDevice = new Device();
        inputDevice.setName("新设备");
        inputDevice.setType("light");
        inputDevice.setProtocol("wifi");
        inputDevice.setMacAddress("11:22:33:44:55:66");

        when(deviceRepository.findByDeviceId(any())).thenReturn(Optional.empty());
        when(deviceRepository.findByMacAddress("11:22:33:44:55:66")).thenReturn(Optional.empty());
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device d = invocation.getArgument(0);
            d.setId(1L);
            return d;
        });

        Device result = deviceService.registerDevice(inputDevice);

        assertNotNull(result);
        assertNotNull(result.getDeviceId());
        assertTrue(result.getDeviceId().startsWith("device-"));
        assertEquals("新设备", result.getName());
        assertEquals("light", result.getType());
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void testRegisterDevice_DuplicateDeviceId() {
        device.setDeviceId("device-12345");
        when(deviceRepository.findByDeviceId("device-12345")).thenReturn(Optional.of(device));

        Device inputDevice = new Device();
        inputDevice.setDeviceId("device-12345");
        inputDevice.setName("重复设备");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deviceService.registerDevice(inputDevice));
        assertTrue(exception.getMessage().contains("设备已存在"));
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    void testRegisterDevice_DuplicateMacAddress() {
        Device existingDevice = new Device();
        existingDevice.setDeviceId("device-existing");
        existingDevice.setMacAddress("AA:BB:CC:DD:EE:FF");

        when(deviceRepository.findByDeviceId(any())).thenReturn(Optional.empty());
        when(deviceRepository.findByMacAddress("AA:BB:CC:DD:EE:FF")).thenReturn(Optional.of(existingDevice));

        Device inputDevice = new Device();
        inputDevice.setName("重复MAC设备");
        inputDevice.setType("light");
        inputDevice.setProtocol("wifi");
        inputDevice.setMacAddress("AA:BB:CC:DD:EE:FF");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deviceService.registerDevice(inputDevice));
        assertTrue(exception.getMessage().contains("MAC地址已存在"));
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    void testRegisterDevice_WithProvidedDeviceId() {
        String providedDeviceId = "my-custom-device-001";
        device.setDeviceId(providedDeviceId);

        when(deviceRepository.findByDeviceId(providedDeviceId)).thenReturn(Optional.empty());
        when(deviceRepository.findByMacAddress(any())).thenReturn(Optional.empty());
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        Device result = deviceService.registerDevice(device);

        assertNotNull(result);
        assertEquals(providedDeviceId, result.getDeviceId());
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void testGetDevice() {
        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));

        Optional<Device> result = deviceService.getDevice("device-001");

        assertTrue(result.isPresent());
        assertEquals("测试设备", result.get().getName());
        verify(deviceRepository).findByDeviceId("device-001");
    }

    @Test
    void testGetAllDevices() {
        Device device2 = new Device();
        device2.setId(2L);
        device2.setName("设备2");
        when(deviceRepository.findAll()).thenReturn(Arrays.asList(device, device2));

        List<Device> result = deviceService.getAllDevices();

        assertEquals(2, result.size());
        verify(deviceRepository).findAll();
    }

    @Test
    void testDeleteDevice_Success() {
        when(deviceRepository.existsById(1L)).thenReturn(true);

        deviceService.deleteDevice(1L);

        verify(deviceRepository).deleteById(1L);
    }

    @Test
    void testDeleteDevice_NotFound() {
        when(deviceRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deviceService.deleteDevice(99L));
        assertTrue(exception.getMessage().contains("设备不存在"));
        verify(deviceRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteDeviceByDeviceId_Success() {
        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));

        deviceService.deleteDeviceByDeviceId("device-001");

        verify(deviceRepository).delete(device);
    }

    @Test
    void testDeleteDeviceByDeviceId_NotFound() {
        when(deviceRepository.findByDeviceId("device-nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deviceService.deleteDeviceByDeviceId("device-nonexistent"));
        assertTrue(exception.getMessage().contains("设备不存在"));
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void testUpdateDeviceStatus() {
        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        deviceService.updateDeviceStatus("device-001", "online");

        verify(deviceRepository).save(any(Device.class));
        assertEquals("online", device.getStatus());
        assertNotNull(device.getUpdatedAt());
    }

    @Test
    void testProcessHeartbeat() {
        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        DeviceHeartbeat heartbeat = new DeviceHeartbeat();
        heartbeat.setCpuUsage(25.5);
        heartbeat.setMemoryUsage(45.2);

        deviceService.processHeartbeat("device-001", heartbeat);

        verify(deviceRepository).save(any(Device.class));
        assertEquals("online", device.getStatus());
        assertNotNull(device.getLastHeartbeat());
    }

    @Test
    void testSendCommand_Success() {
        device.setDeviceId("device-001");
        device.setProtocol("wifi");
        device.setType("light");

        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);
        when(wifiAdapter.sendCommand(any(Device.class), eq("on"))).thenReturn(true);

        boolean result = deviceService.sendCommand("device-001", "on");

        assertTrue(result);
        verify(wifiAdapter).sendCommand(device, "on");
        verify(analyticsClient).recordEnergy(device, "on");
        verify(analyticsClient).recordBehavior(device, "on", "1", "user");
    }

    @Test
    void testSendCommand_DeviceNotFound() {
        when(deviceRepository.findByDeviceId("device-nonexistent")).thenReturn(Optional.empty());

        boolean result = deviceService.sendCommand("device-nonexistent", "on");

        assertFalse(result);
        verify(wifiAdapter, never()).sendCommand(any(), any());
        verify(analyticsClient, never()).recordEnergy(any(), any());
    }

    @Test
    void testSendCommand_UnknownProtocol() {
        device.setDeviceId("device-001");
        device.setProtocol("unknown-protocol");
        device.setType("sensor");

        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        boolean result = deviceService.sendCommand("device-001", "on");

        assertTrue(result);
        verify(analyticsClient).recordEnergy(device, "on");
        verify(analyticsClient).recordBehavior(device, "on", "1", "user");
    }

    @Test
    void testSendCommand_NullProtocol() {
        device.setDeviceId("device-001");
        device.setProtocol(null);
        device.setType("sensor");

        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);
        when(wifiAdapter.sendCommand(any(Device.class), eq("on"))).thenReturn(true);

        boolean result = deviceService.sendCommand("device-001", "on");

        assertTrue(result);
        verify(wifiAdapter).sendCommand(device, "on");
        verify(analyticsClient).recordEnergy(device, "on");
        verify(analyticsClient).recordBehavior(device, "on", "1", "user");
    }

    @Test
    void testGetDeviceStatistics() {
        when(deviceRepository.count()).thenReturn(10L);
        when(deviceRepository.countOnlineDevices()).thenReturn(5L);
        when(deviceRepository.countOfflineDevices()).thenReturn(3L);
        when(deviceRepository.countWarningDevices()).thenReturn(2L);

        Map<String, Long> statistics = deviceService.getDeviceStatistics();

        assertEquals(10L, statistics.get("total"));
        assertEquals(5L, statistics.get("online"));
        assertEquals(3L, statistics.get("offline"));
        assertEquals(2L, statistics.get("warning"));
    }

    @Test
    void testCheckDeviceOnlineStatus() {
        Device onlineDevice = new Device();
        onlineDevice.setId(1L);
        onlineDevice.setDeviceId("device-001");
        onlineDevice.setStatus("online");

        when(deviceRepository.findDevicesWithExpiredHeartbeat(any(LocalDateTime.class)))
                .thenReturn(List.of(onlineDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(onlineDevice);

        deviceService.checkDeviceOnlineStatus();

        assertEquals("offline", onlineDevice.getStatus());
        verify(deviceRepository).save(onlineDevice);
    }

    @Test
    void testApplyStatusByCommand_On() {
        device.setDeviceId("device-001");
        device.setProtocol("wifi");
        device.setType("light");

        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);
        when(wifiAdapter.sendCommand(any(Device.class), eq("on"))).thenReturn(true);

        deviceService.sendCommand("device-001", "on");

        assertEquals("online", device.getStatus());
    }

    @Test
    void testApplyStatusByCommand_Off() {
        device.setDeviceId("device-001");
        device.setProtocol("wifi");
        device.setType("light");

        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);
        when(wifiAdapter.sendCommand(any(Device.class), eq("off"))).thenReturn(true);

        deviceService.sendCommand("device-001", "off");

        assertEquals("offline", device.getStatus());
    }

    @Test
    void testApplyStatusByCommand_Dim() {
        device.setDeviceId("device-001");
        device.setProtocol("wifi");
        device.setType("light");

        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);
        when(wifiAdapter.sendCommand(any(Device.class), eq("dim"))).thenReturn(true);

        deviceService.sendCommand("device-001", "dim");

        assertEquals("online", device.getStatus());
    }

    @Test
    void testApplyStatusByCommand_Null() {
        device.setDeviceId("device-001");
        device.setProtocol("wifi");
        device.setType("light");
        device.setStatus("online");

        when(deviceRepository.findByDeviceId("device-001")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);
        when(wifiAdapter.sendCommand(any(Device.class), isNull())).thenReturn(true);

        deviceService.sendCommand("device-001", null);

        assertEquals("online", device.getStatus());
    }
}