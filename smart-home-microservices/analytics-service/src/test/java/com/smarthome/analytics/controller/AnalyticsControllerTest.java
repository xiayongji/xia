package com.smarthome.analytics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.analytics.entity.AnomalyDetection;
import com.smarthome.analytics.entity.EnergyConsumption;
import com.smarthome.analytics.service.AnomalyDetectionService;
import com.smarthome.analytics.service.EnergyStatisticsService;
import com.smarthome.analytics.service.UserBehaviorAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnergyStatisticsService energyService;

    @MockBean
    private AnomalyDetectionService anomalyService;

    @MockBean
    private UserBehaviorAnalysisService behaviorService;

    @Test
    void testSaveEnergyData() throws Exception {
        EnergyConsumption consumption = new EnergyConsumption();
        consumption.setId(1L);
        consumption.setDeviceId("device-001");
        consumption.setDeviceName("空调");
        consumption.setPower(1500.0);
        consumption.setEnergy(30.5);
        consumption.setDeviceType("空调");
        consumption.setTimestamp(LocalDateTime.now());

        when(energyService.saveEnergyData(anyString(), anyString(), anyDouble(), anyDouble(), anyString()))
                .thenReturn(consumption);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("deviceId", "device-001");
        requestBody.put("deviceName", "空调");
        requestBody.put("power", 1500.0);
        requestBody.put("energy", 30.5);
        requestBody.put("deviceType", "空调");

        mockMvc.perform(post("/api/analytics/energy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId").value("device-001"))
                .andExpect(jsonPath("$.deviceName").value("空调"))
                .andExpect(jsonPath("$.power").value(1500.0))
                .andExpect(jsonPath("$.energy").value(30.5));

        verify(energyService, times(1)).saveEnergyData(anyString(), anyString(), anyDouble(), anyDouble(), anyString());
    }

    @Test
    void testGetTodayEnergyStatistics() throws Exception {
        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("totalEnergy", 100.0);
        statistics.put("energy", 100.0);
        statistics.put("totalCost", 50.0);
        statistics.put("deviceEnergyList", List.of());

        when(energyService.getTodayEnergyStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/analytics/energy/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEnergy").value(100.0))
                .andExpect(jsonPath("$.energy").value(100.0))
                .andExpect(jsonPath("$.totalCost").value(50.0));

        verify(energyService, times(1)).getTodayEnergyStatistics();
    }

    @Test
    void testGetEnergyStatistics() throws Exception {
        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("totalEnergy", 200.0);

        when(energyService.getEnergyStatistics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(statistics);

        mockMvc.perform(get("/api/analytics/energy/stats")
                        .param("startTime", "2024-01-01T00:00:00")
                        .param("endTime", "2024-01-07T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEnergy").value(200.0));

        verify(energyService, times(1)).getEnergyStatistics(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetDeviceEnergyHistory() throws Exception {
        EnergyConsumption ec = new EnergyConsumption();
        ec.setId(1L);
        ec.setDeviceId("device-001");
        ec.setDeviceName("空调");
        ec.setPower(1500.0);
        ec.setEnergy(30.5);
        ec.setTimestamp(LocalDateTime.now());

        when(energyService.getDeviceEnergyHistory(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(ec));

        mockMvc.perform(get("/api/analytics/energy/device/{deviceId}", "device-001")
                        .param("startTime", "2024-01-01T00:00:00")
                        .param("endTime", "2024-01-07T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("device-001"))
                .andExpect(jsonPath("$[0].deviceName").value("空调"));

        verify(energyService, times(1)).getDeviceEnergyHistory(anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetEnergyTrend() throws Exception {
        Map<String, Object> dayStats = new LinkedHashMap<>();
        dayStats.put("date", "2024-01-01");
        dayStats.put("energy", 50.0);

        when(energyService.getEnergyTrend(anyInt())).thenReturn(List.of(dayStats));

        mockMvc.perform(get("/api/analytics/energy/trend")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2024-01-01"))
                .andExpect(jsonPath("$[0].energy").value(50.0));

        verify(energyService, times(1)).getEnergyTrend(7);
    }

    @Test
    void testGetHighEnergyDevices() throws Exception {
        Map<String, Object> deviceStat = new LinkedHashMap<>();
        deviceStat.put("deviceId", "device-001");
        deviceStat.put("deviceName", "device-001");
        deviceStat.put("totalEnergy", 150.0);

        when(energyService.getHighEnergyDevices(anyInt())).thenReturn(List.of(deviceStat));

        mockMvc.perform(get("/api/analytics/energy/high-energy-devices")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("device-001"))
                .andExpect(jsonPath("$[0].totalEnergy").value(150.0));

        verify(energyService, times(1)).getHighEnergyDevices(3);
    }

    @Test
    void testDetectAnomaly_AnomalyFound() throws Exception {
        AnomalyDetection anomaly = new AnomalyDetection();
        anomaly.setId(1L);
        anomaly.setDeviceId("device-001");
        anomaly.setDeviceName("空调");
        anomaly.setAnomalyType("out_of_range");
        anomaly.setConfidence(0.98);
        anomaly.setStatus("pending");

        when(anomalyService.detectAnomaly(anyString(), anyString(), anyString(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(anomaly);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("deviceId", "device-001");
        requestBody.put("deviceName", "空调");
        requestBody.put("metricType", "温度");
        requestBody.put("value", 40.0);
        requestBody.put("normalMin", 18.0);
        requestBody.put("normalMax", 30.0);

        mockMvc.perform(post("/api/analytics/anomaly/detect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId").value("device-001"))
                .andExpect(jsonPath("$.anomalyType").value("out_of_range"))
                .andExpect(jsonPath("$.confidence").value(0.98));

        verify(anomalyService, times(1)).detectAnomaly(anyString(), anyString(), anyString(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void testDetectAnomaly_NoAnomalyFound() throws Exception {
        when(anomalyService.detectAnomaly(anyString(), anyString(), anyString(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(null);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("deviceId", "device-001");
        requestBody.put("deviceName", "空调");
        requestBody.put("metricType", "温度");
        requestBody.put("value", 25.0);
        requestBody.put("normalMin", 18.0);
        requestBody.put("normalMax", 30.0);

        mockMvc.perform(post("/api/analytics/anomaly/detect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("未检测到异常"));

        verify(anomalyService, times(1)).detectAnomaly(anyString(), anyString(), anyString(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void testGetDeviceAnomalies() throws Exception {
        AnomalyDetection anomaly = new AnomalyDetection();
        anomaly.setId(1L);
        anomaly.setDeviceId("device-001");
        anomaly.setAnomalyType("out_of_range");
        anomaly.setConfidence(0.98);
        anomaly.setStatus("pending");

        when(anomalyService.getDeviceAnomalies("device-001")).thenReturn(List.of(anomaly));

        mockMvc.perform(get("/api/analytics/anomaly/device/{deviceId}", "device-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("device-001"))
                .andExpect(jsonPath("$[0].anomalyType").value("out_of_range"));

        verify(anomalyService, times(1)).getDeviceAnomalies("device-001");
    }

    @Test
    void testGetPendingAnomalies() throws Exception {
        AnomalyDetection anomaly = new AnomalyDetection();
        anomaly.setId(1L);
        anomaly.setDeviceId("device-001");
        anomaly.setAnomalyType("out_of_range");
        anomaly.setConfidence(0.98);
        anomaly.setStatus("pending");

        when(anomalyService.getPendingAnomalies()).thenReturn(List.of(anomaly));

        mockMvc.perform(get("/api/analytics/anomaly/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("pending"));

        verify(anomalyService, times(1)).getPendingAnomalies();
    }

    @Test
    void testGetHighConfidenceAnomalies() throws Exception {
        AnomalyDetection anomaly = new AnomalyDetection();
        anomaly.setId(1L);
        anomaly.setDeviceId("device-001");
        anomaly.setAnomalyType("statistical_anomaly");
        anomaly.setConfidence(0.99);
        anomaly.setStatus("pending");

        when(anomalyService.getHighConfidenceAnomalies()).thenReturn(List.of(anomaly));

        mockMvc.perform(get("/api/analytics/anomaly/high-confidence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].confidence").value(0.99));

        verify(anomalyService, times(1)).getHighConfidenceAnomalies();
    }

    @Test
    void testResolveAnomaly() throws Exception {
        doNothing().when(anomalyService).resolveAnomaly(anyLong());

        mockMvc.perform(put("/api/analytics/anomaly/{anomalyId}/resolve", 1L))
                .andExpect(status().isOk());

        verify(anomalyService, times(1)).resolveAnomaly(1L);
    }

    @Test
    void testGetAnomalyStatistics() throws Exception {
        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("totalPending", 2L);

        when(anomalyService.getAnomalyStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/analytics/anomaly/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(2));

        verify(anomalyService, times(1)).getAnomalyStatistics();
    }

    @Test
    void testRecordBehavior() throws Exception {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("userId", "user-001");
        requestBody.put("username", "张三");
        requestBody.put("behaviorType", "device_control");
        requestBody.put("deviceId", "device-001");
        requestBody.put("deviceName", "空调");
        requestBody.put("action", "turn_on");
        requestBody.put("category", "温度控制");

        mockMvc.perform(post("/api/analytics/behavior")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated());

        verify(behaviorService, times(1)).recordBehavior(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testHealth() throws Exception {
        mockMvc.perform(get("/api/analytics/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("analytics-service"));
    }
}