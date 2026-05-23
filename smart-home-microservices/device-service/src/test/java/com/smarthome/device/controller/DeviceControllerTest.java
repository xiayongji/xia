package com.smarthome.device.controller;

import com.smarthome.device.entity.Device;
import com.smarthome.device.entity.DeviceHeartbeat;
import com.smarthome.device.entity.DeviceShadow;
import com.smarthome.device.service.DeviceService;
import com.smarthome.device.service.DeviceShadowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceService deviceService;

    @MockBean
    private DeviceShadowService shadowService;

    @Test
    void testRegisterDevice() throws Exception {
        Device device = createDevice();
        when(deviceService.registerDevice(any(Device.class))).thenReturn(device);

        mockMvc.perform(post("/api/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(device)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId").value("device-001"))
                .andExpect(jsonPath("$.name").value("测试设备"));

        verify(deviceService).registerDevice(any(Device.class));
    }

    @Test
    void testAddDevice() throws Exception {
        Device device = createDevice();
        when(deviceService.registerDevice(any(Device.class))).thenReturn(device);

        Map<String, String> deviceData = Map.of(
                "name", "测试设备",
                "type", "light",
                "protocol", "wifi",
                "manufacturer", "测试厂商",
                "model", "TEST-001"
        );

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId").value("device-001"));

        verify(deviceService).registerDevice(any(Device.class));
    }

    @Test
    void testGetAllDevices() throws Exception {
        Device device = createDevice();
        when(deviceService.getAllDevices()).thenReturn(List.of(device));

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("device-001"))
                .andExpect(jsonPath("$[0].name").value("测试设备"));

        verify(deviceService).getAllDevices();
    }

    @Test
    void testGetDevice() throws Exception {
        Device device = createDevice();
        when(deviceService.getDevice("device-001")).thenReturn(Optional.of(device));

        mockMvc.perform(get("/api/devices/device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("device-001"))
                .andExpect(jsonPath("$.name").value("测试设备"));

        verify(deviceService).getDevice("device-001");
    }

    @Test
    void testGetDevice_NotFound() throws Exception {
        when(deviceService.getDevice("device-nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/devices/device-nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateDeviceStatus() throws Exception {
        Device device = createDevice();
        when(deviceService.getDevice("device-001")).thenReturn(Optional.of(device));
        doNothing().when(deviceService).updateDeviceStatus(eq("device-001"), eq("online"));

        mockMvc.perform(put("/api/devices/device-001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"online\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("device-001"));

        verify(deviceService).updateDeviceStatus("device-001", "online");
    }

    @Test
    void testUpdateDeviceStatus_EmptyStatus() throws Exception {
        mockMvc.perform(put("/api/devices/device-001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testProcessHeartbeat() throws Exception {
        doNothing().when(deviceService).processHeartbeat(eq("device-001"), any(DeviceHeartbeat.class));

        mockMvc.perform(post("/api/devices/device-001/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpuUsage\":25.5,\"memoryUsage\":45.2}"))
                .andExpect(status().isOk());

        verify(deviceService).processHeartbeat(eq("device-001"), any(DeviceHeartbeat.class));
    }

    @Test
    void testSendCommand() throws Exception {
        when(deviceService.sendCommand("device-001", "on")).thenReturn(true);

        mockMvc.perform(post("/api/devices/device-001/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"on\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("命令发送成功"));

        verify(deviceService).sendCommand("device-001", "on");
    }

    @Test
    void testSendCommand_Failure() throws Exception {
        when(deviceService.sendCommand("device-001", "on")).thenReturn(false);

        mockMvc.perform(post("/api/devices/device-001/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"on\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("命令发送失败"));
    }

    @Test
    void testSendCommand_EmptyCommand() throws Exception {
        mockMvc.perform(post("/api/devices/device-001/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetDeviceStatistics() throws Exception {
        Map<String, Long> statistics = Map.of("total", 10L, "online", 5L, "offline", 3L, "warning", 2L);
        when(deviceService.getDeviceStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/devices/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.online").value(5))
                .andExpect(jsonPath("$.offline").value(3))
                .andExpect(jsonPath("$.warning").value(2));

        verify(deviceService).getDeviceStatistics();
    }

    @Test
    void testDeleteDevice() throws Exception {
        doNothing().when(deviceService).deleteDeviceByDeviceId("device-001");

        mockMvc.perform(delete("/api/devices/device-001"))
                .andExpect(status().isNoContent());

        verify(deviceService).deleteDeviceByDeviceId("device-001");
    }

    @Test
    void testDeleteDevice_NotFound() throws Exception {
        doThrow(new RuntimeException("设备不存在: device-nonexistent"))
                .when(deviceService).deleteDeviceByDeviceId("device-nonexistent");

        mockMvc.perform(delete("/api/devices/device-nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDeviceShadow() throws Exception {
        DeviceShadow shadow = createShadow();
        when(shadowService.getShadow("device-001")).thenReturn(Optional.of(shadow));

        mockMvc.perform(get("/api/devices/device-001/shadow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("device-001"))
                .andExpect(jsonPath("$.desiredState").value("{\"power\":\"on\"}"));

        verify(shadowService).getShadow("device-001");
    }

    @Test
    void testGetDeviceShadow_NotFound() throws Exception {
        when(shadowService.getShadow("device-nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/devices/device-nonexistent/shadow"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateDesiredState() throws Exception {
        doNothing().when(shadowService).updateDesiredState(anyString(), any(Map.class));

        mockMvc.perform(put("/api/devices/device-001/shadow/desired")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"power\":\"on\",\"brightness\":100}"))
                .andExpect(status().isOk());

        verify(shadowService).updateDesiredState(eq("device-001"), any(Map.class));
    }

    @Test
    void testUpdateReportedState() throws Exception {
        doNothing().when(shadowService).updateReportedState(anyString(), any(Map.class));

        mockMvc.perform(put("/api/devices/device-001/shadow/reported")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"power\":\"on\",\"temperature\":25.5}"))
                .andExpect(status().isOk());

        verify(shadowService).updateReportedState(eq("device-001"), any(Map.class));
    }

    @Test
    void testCheckStateSync() throws Exception {
        when(shadowService.isStateSynchronized("device-001")).thenReturn(true);

        mockMvc.perform(get("/api/devices/device-001/shadow/sync"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(shadowService).isStateSynchronized("device-001");
    }

    @Test
    void testGetStateDiff() throws Exception {
        Map<String, Object> diff = Map.of("brightness", 100, "color", "red");
        when(shadowService.getStateDiff("device-001")).thenReturn(diff);

        mockMvc.perform(get("/api/devices/device-001/shadow/diff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brightness").value(100))
                .andExpect(jsonPath("$.color").value("red"));

        verify(shadowService).getStateDiff("device-001");
    }

    @Test
    void testUpdateDeviceStatus_DeviceNotFound() throws Exception {
        when(deviceService.getDevice("device-nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/devices/device-nonexistent/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"online\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("设备不存在"));
    }

    private Device createDevice() {
        Device device = new Device();
        device.setId(1L);
        device.setDeviceId("device-001");
        device.setName("测试设备");
        device.setType("light");
        device.setProtocol("wifi");
        device.setStatus("offline");
        device.setManufacturer("测试厂商");
        device.setModel("TEST-001");
        device.setCreatedAt(LocalDateTime.now());
        return device;
    }

    private DeviceShadow createShadow() {
        DeviceShadow shadow = new DeviceShadow();
        shadow.setId(1L);
        shadow.setDeviceId("device-001");
        shadow.setDesiredState("{\"power\":\"on\"}");
        shadow.setReportedState("{\"power\":\"on\"}");
        shadow.setVersion(1);
        shadow.setLastUpdated(LocalDateTime.now());
        return shadow;
    }
}