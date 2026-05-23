package com.smarthome.edge.controller;

import com.smarthome.edge.EdgeGatewayService;
import com.smarthome.edge.entity.CommandResult;
import com.smarthome.edge.entity.Device;
import com.smarthome.edge.entity.DeviceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EdgeGatewayController.class)
@AutoConfigureMockMvc(addFilters = false)
class EdgeGatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EdgeGatewayService edgeGatewayService;

    private static final String DEVICE_ID = "device-001";

    @BeforeEach
    void setUp() {
    }

    @Test
    void testControlDevice_Success() throws Exception {
        CommandResult result = CommandResult.builder()
                .deviceId(DEVICE_ID)
                .success(true)
                .message("命令执行成功")
                .timestamp(LocalDateTime.now())
                .build();

        when(edgeGatewayService.handleControlCommand(eq(DEVICE_ID), eq("turn_on"))).thenReturn(result);

        mockMvc.perform(post("/api/edge/devices/{deviceId}/command", DEVICE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"turn_on\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(DEVICE_ID))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("命令执行成功"));
    }

    @Test
    void testControlDevice_MissingCommand() throws Exception {
        mockMvc.perform(post("/api/edge/devices/{deviceId}/command", DEVICE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.deviceId").value(DEVICE_ID))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("命令不能为空"));

        verify(edgeGatewayService, never()).handleControlCommand(anyString(), anyString());
    }

    @Test
    void testGetDeviceStatus_Found() throws Exception {
        DeviceStatus status = DeviceStatus.builder()
                .deviceId(DEVICE_ID)
                .status("online")
                .power(100.0)
                .lastUpdateTime(LocalDateTime.now())
                .build();

        when(edgeGatewayService.getDeviceStatus(DEVICE_ID)).thenReturn(status);

        mockMvc.perform(get("/api/edge/devices/{deviceId}/status", DEVICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(DEVICE_ID))
                .andExpect(jsonPath("$.status").value("online"))
                .andExpect(jsonPath("$.power").value(100.0));
    }

    @Test
    void testGetDeviceStatus_NotFound() throws Exception {
        when(edgeGatewayService.getDeviceStatus(DEVICE_ID)).thenReturn(null);

        mockMvc.perform(get("/api/edge/devices/{deviceId}/status", DEVICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(DEVICE_ID))
                .andExpect(jsonPath("$.status").value("not_found"))
                .andExpect(jsonPath("$.properties").value("设备不存在或未查询到状态"));
    }

    @Test
    void testGetAllDevices() throws Exception {
        List<Device> devices = Arrays.asList(
                Device.builder().deviceId("device-001").deviceType("light").protocol("wifi").build(),
                Device.builder().deviceId("device-002").deviceType("sensor").protocol("zigbee").build()
        );

        when(edgeGatewayService.getAllLocalDevices()).thenReturn(devices);

        mockMvc.perform(get("/api/edge/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("device-001"))
                .andExpect(jsonPath("$[0].deviceType").value("light"))
                .andExpect(jsonPath("$[0].protocol").value("wifi"))
                .andExpect(jsonPath("$[1].deviceId").value("device-002"))
                .andExpect(jsonPath("$[1].deviceType").value("sensor"))
                .andExpect(jsonPath("$[1].protocol").value("zigbee"));
    }

    @Test
    void testGetLocalDeviceIds() throws Exception {
        List<String> deviceIds = Arrays.asList("device-001", "device-002", "device-003");

        when(edgeGatewayService.getLocalDeviceIds()).thenReturn(deviceIds);

        mockMvc.perform(get("/api/edge/devices/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("device-001"))
                .andExpect(jsonPath("$[1]").value("device-002"))
                .andExpect(jsonPath("$[2]").value("device-003"));
    }

    @Test
    void testGetAllDeviceStatuses() throws Exception {
        List<DeviceStatus> statuses = Arrays.asList(
                DeviceStatus.builder().deviceId("device-001").status("online").lastUpdateTime(LocalDateTime.now()).build(),
                DeviceStatus.builder().deviceId("device-002").status("offline").lastUpdateTime(LocalDateTime.now()).build()
        );

        when(edgeGatewayService.getAllDeviceStatuses()).thenReturn(statuses);

        mockMvc.perform(get("/api/edge/devices/status/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("device-001"))
                .andExpect(jsonPath("$[0].status").value("online"))
                .andExpect(jsonPath("$[1].deviceId").value("device-002"))
                .andExpect(jsonPath("$[1].status").value("offline"));
    }

    @Test
    void testRegisterDevice_Success() throws Exception {
        when(edgeGatewayService.registerLocalDevice(any(Device.class))).thenReturn(true);

        mockMvc.perform(post("/api/edge/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceId\":\"device-001\",\"deviceType\":\"light\",\"protocol\":\"wifi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("设备注册成功"))
                .andExpect(jsonPath("$.deviceId").value("device-001"));
    }

    @Test
    void testRegisterDevice_MissingDeviceId() throws Exception {
        mockMvc.perform(post("/api/edge/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceType\":\"light\",\"protocol\":\"wifi\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("设备ID不能为空"));

        verify(edgeGatewayService, never()).registerLocalDevice(any(Device.class));
    }

    @Test
    void testUnregisterDevice() throws Exception {
        when(edgeGatewayService.unregisterLocalDevice(DEVICE_ID)).thenReturn(true);

        mockMvc.perform(delete("/api/edge/devices/{deviceId}/unregister", DEVICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("设备注销成功"))
                .andExpect(jsonPath("$.deviceId").value(DEVICE_ID));
    }

    @Test
    void testUpdateDeviceStatus() throws Exception {
        doNothing().when(edgeGatewayService).updateDeviceStatus(
                eq(DEVICE_ID), eq("online"), eq(150.0), eq(25.5), eq(60.0), anyString());

        mockMvc.perform(put("/api/edge/devices/{deviceId}/status", DEVICE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"online\",\"power\":150.0,\"temperature\":25.5,\"humidity\":60.0,\"properties\":\"{\\\"brightness\\\":80}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("设备状态更新成功"))
                .andExpect(jsonPath("$.deviceId").value(DEVICE_ID));
    }

    @Test
    void testGetDeviceStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDevices", 10L);
        stats.put("onlineDevices", 5L);
        stats.put("offlineDevices", 5L);
        stats.put("totalPowerConsumption", 500.0);

        when(edgeGatewayService.getDeviceStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/edge/devices/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDevices").value(10))
                .andExpect(jsonPath("$.onlineDevices").value(5))
                .andExpect(jsonPath("$.offlineDevices").value(5))
                .andExpect(jsonPath("$.totalPowerConsumption").value(500.0));
    }

    @Test
    void testGetLatencyMetrics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDevices", 10L);
        stats.put("onlineDevices", 5L);
        stats.put("offlineDevices", 5L);
        stats.put("totalPowerConsumption", 500.0);
        stats.put("lastSyncTime", LocalDateTime.now());

        when(edgeGatewayService.getDeviceStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/edge/metrics/latency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.edge_processing_time_ms").value("<50"))
                .andExpect(jsonPath("$.local_command_success_rate").value("99.9%"))
                .andExpect(jsonPath("$.cloud_sync_frequency").value("10s"))
                .andExpect(jsonPath("$.supported_protocols").isArray())
                .andExpect(jsonPath("$.total_devices").value(10))
                .andExpect(jsonPath("$.online_devices").value(5))
                .andExpect(jsonPath("$.offline_devices").value(5))
                .andExpect(jsonPath("$.total_power_consumption").value(500.0))
                .andExpect(jsonPath("$.database_status").value("connected"));
    }

    @Test
    void testHealthCheck() throws Exception {
        List<DeviceStatus> statuses = Collections.singletonList(
                DeviceStatus.builder().deviceId("device-001").status("online").lastUpdateTime(LocalDateTime.now()).build()
        );

        when(edgeGatewayService.getAllDeviceStatuses()).thenReturn(statuses);

        mockMvc.perform(get("/api/edge/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("edge-service"))
                .andExpect(jsonPath("$.database").value("connected"))
                .andExpect(jsonPath("$.deviceCount").value(1));
    }

    @Test
    void testGetDevicesByType() throws Exception {
        List<Device> devices = Arrays.asList(
                Device.builder().deviceId("device-001").deviceType("light").protocol("wifi").build(),
                Device.builder().deviceId("device-003").deviceType("light").protocol("zigbee").build()
        );

        when(edgeGatewayService.getAllLocalDevices()).thenReturn(devices);

        mockMvc.perform(get("/api/edge/devices/type/light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("device-001"))
                .andExpect(jsonPath("$[0].deviceType").value("light"))
                .andExpect(jsonPath("$[1].deviceId").value("device-003"))
                .andExpect(jsonPath("$[1].deviceType").value("light"));
    }

    @Test
    void testGetDevicesByProtocol() throws Exception {
        List<Device> devices = Arrays.asList(
                Device.builder().deviceId("device-001").deviceType("light").protocol("wifi").build(),
                Device.builder().deviceId("device-002").deviceType("sensor").protocol("wifi").build()
        );

        when(edgeGatewayService.getAllLocalDevices()).thenReturn(devices);

        mockMvc.perform(get("/api/edge/devices/protocol/wifi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("device-001"))
                .andExpect(jsonPath("$[0].protocol").value("wifi"))
                .andExpect(jsonPath("$[1].deviceId").value("device-002"))
                .andExpect(jsonPath("$[1].protocol").value("wifi"));
    }
}