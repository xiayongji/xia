package com.smarthome.analytics.service;

import com.smarthome.analytics.entity.AnomalyDetection;
import com.smarthome.analytics.repository.AnomalyDetectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    @Mock
    private AnomalyDetectionRepository anomalyRepository;

    @InjectMocks
    private AnomalyDetectionService anomalyDetectionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(anomalyDetectionService, "anomalyThreshold", 0.95);
    }

    @Test
    void testDetectAnomaly_OutOfRange() {
        AnomalyDetection expectedAnomaly = new AnomalyDetection();
        expectedAnomaly.setId(1L);
        expectedAnomaly.setDeviceId("device-001");
        expectedAnomaly.setDeviceName("空调");
        expectedAnomaly.setAnomalyType("out_of_range");
        expectedAnomaly.setConfidence(0.975);
        expectedAnomaly.setDetails("检测到温度异常: 当前值=40.00, 正常范围=[18.00, 30.00]");
        expectedAnomaly.setStatus("pending");
        expectedAnomaly.setDetectedAt(LocalDateTime.now());

        when(anomalyRepository.save(any(AnomalyDetection.class))).thenReturn(expectedAnomaly);

        AnomalyDetection result = anomalyDetectionService.detectAnomaly(
                "device-001", "空调", "温度", 40.0, 18.0, 30.0);

        assertNotNull(result);
        assertEquals("device-001", result.getDeviceId());
        assertEquals("out_of_range", result.getAnomalyType());
        assertEquals("pending", result.getStatus());
        assertTrue(result.getConfidence() >= 0.95);
        verify(anomalyRepository, times(1)).save(any(AnomalyDetection.class));
    }

    @Test
    void testDetectAnomaly_WithinRange() {
        AnomalyDetection result = anomalyDetectionService.detectAnomaly(
                "device-001", "空调", "温度", 25.0, 18.0, 30.0);

        assertNull(result);
        verify(anomalyRepository, never()).save(any(AnomalyDetection.class));
    }

    @Test
    void testDetectAnomaly_BelowThreshold() {
        AnomalyDetection result = anomalyDetectionService.detectAnomaly(
                "device-001", "空调", "温度", 30.5, 18.0, 30.0);

        assertNull(result);
        verify(anomalyRepository, never()).save(any(AnomalyDetection.class));
    }

    @Test
    void testDetectAnomalyWithStatistics_ZScoreHigh() {
        List<Double> recentValues = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            recentValues.add(50.0);
        }
        recentValues.add(200.0);

        AnomalyDetection expectedAnomaly = new AnomalyDetection();
        expectedAnomaly.setId(1L);
        expectedAnomaly.setDeviceId("device-001");
        expectedAnomaly.setDeviceName("空调");
        expectedAnomaly.setAnomalyType("statistical_anomaly");
        expectedAnomaly.setConfidence(0.97);
        expectedAnomaly.setStatus("pending");

        when(anomalyRepository.save(any(AnomalyDetection.class))).thenReturn(expectedAnomaly);

        AnomalyDetection result = anomalyDetectionService.detectAnomalyWithStatistics(
                "device-001", "空调", recentValues, "功率");

        assertNotNull(result);
        assertEquals("device-001", result.getDeviceId());
        assertEquals("statistical_anomaly", result.getAnomalyType());
        verify(anomalyRepository, times(1)).save(any(AnomalyDetection.class));
    }

    @Test
    void testDetectAnomalyWithStatistics_ZScoreNormal() {
        List<Double> recentValues = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            recentValues.add(50.0 + Math.random() * 5);
        }

        AnomalyDetection result = anomalyDetectionService.detectAnomalyWithStatistics(
                "device-001", "空调", recentValues, "功率");

        assertNull(result);
        verify(anomalyRepository, never()).save(any(AnomalyDetection.class));
    }

    @Test
    void testDetectAnomalyWithStatistics_InsufficientData() {
        List<Double> recentValues = List.of(50.0, 51.0, 52.0);

        AnomalyDetection result = anomalyDetectionService.detectAnomalyWithStatistics(
                "device-001", "空调", recentValues, "功率");

        assertNull(result);
        verify(anomalyRepository, never()).save(any(AnomalyDetection.class));
    }

    @Test
    void testGetDeviceAnomalies() {
        AnomalyDetection a1 = new AnomalyDetection();
        a1.setId(1L);
        a1.setDeviceId("device-001");
        a1.setAnomalyType("out_of_range");
        a1.setConfidence(0.98);
        a1.setStatus("pending");
        a1.setDetectedAt(LocalDateTime.now().minusHours(1));

        AnomalyDetection a2 = new AnomalyDetection();
        a2.setId(2L);
        a2.setDeviceId("device-001");
        a2.setAnomalyType("statistical_anomaly");
        a2.setConfidence(0.96);
        a2.setStatus("resolved");
        a2.setDetectedAt(LocalDateTime.now().minusHours(2));

        List<AnomalyDetection> expectedList = List.of(a1, a2);

        when(anomalyRepository.findByDeviceIdOrderByDetectedAtDesc("device-001")).thenReturn(expectedList);

        List<AnomalyDetection> result = anomalyDetectionService.getDeviceAnomalies("device-001");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(anomalyRepository, times(1)).findByDeviceIdOrderByDetectedAtDesc("device-001");
    }

    @Test
    void testGetPendingAnomalies() {
        AnomalyDetection a1 = new AnomalyDetection();
        a1.setId(1L);
        a1.setDeviceId("device-001");
        a1.setStatus("pending");

        List<AnomalyDetection> expectedList = List.of(a1);

        when(anomalyRepository.findPendingAnomalies()).thenReturn(expectedList);

        List<AnomalyDetection> result = anomalyDetectionService.getPendingAnomalies();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("pending", result.get(0).getStatus());
        verify(anomalyRepository, times(1)).findPendingAnomalies();
    }

    @Test
    void testGetHighConfidenceAnomalies() {
        AnomalyDetection a1 = new AnomalyDetection();
        a1.setId(1L);
        a1.setDeviceId("device-001");
        a1.setConfidence(0.99);
        a1.setStatus("pending");

        List<AnomalyDetection> expectedList = List.of(a1);

        when(anomalyRepository.findHighConfidenceAnomalies(0.95)).thenReturn(expectedList);

        List<AnomalyDetection> result = anomalyDetectionService.getHighConfidenceAnomalies();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0.99, result.get(0).getConfidence());
        verify(anomalyRepository, times(1)).findHighConfidenceAnomalies(0.95);
    }

    @Test
    void testResolveAnomaly() {
        AnomalyDetection anomaly = new AnomalyDetection();
        anomaly.setId(1L);
        anomaly.setDeviceId("device-001");
        anomaly.setAnomalyType("out_of_range");
        anomaly.setConfidence(0.98);
        anomaly.setStatus("pending");
        anomaly.setDetectedAt(LocalDateTime.now().minusHours(1));

        when(anomalyRepository.findById(1L)).thenReturn(Optional.of(anomaly));
        when(anomalyRepository.save(any(AnomalyDetection.class))).thenReturn(anomaly);

        anomalyDetectionService.resolveAnomaly(1L);

        assertEquals("resolved", anomaly.getStatus());
        assertNotNull(anomaly.getResolvedAt());
        verify(anomalyRepository, times(1)).findById(1L);
        verify(anomalyRepository, times(1)).save(anomaly);
    }

    @Test
    void testGetAnomalyStatistics() {
        List<Object[]> typeStats = new ArrayList<>();
        typeStats.add(new Object[]{"out_of_range", 5L});
        typeStats.add(new Object[]{"statistical_anomaly", 3L});

        List<Object[]> deviceStats = new ArrayList<>();
        deviceStats.add(new Object[]{"device-001", 4L});
        deviceStats.add(new Object[]{"device-002", 2L});

        AnomalyDetection pending1 = new AnomalyDetection();
        pending1.setId(1L);
        pending1.setStatus("pending");
        AnomalyDetection pending2 = new AnomalyDetection();
        pending2.setId(2L);
        pending2.setStatus("pending");
        List<AnomalyDetection> pendingList = List.of(pending1, pending2);

        when(anomalyRepository.countByAnomalyType(any(LocalDateTime.class))).thenReturn(typeStats);
        when(anomalyRepository.countPendingAnomaliesByDevice()).thenReturn(deviceStats);
        when(anomalyRepository.findByStatusOrderByDetectedAtDesc("pending")).thenReturn(pendingList);

        Map<String, Object> statistics = anomalyDetectionService.getAnomalyStatistics();

        assertNotNull(statistics);
        assertNotNull(statistics.get("byType"));
        assertNotNull(statistics.get("pendingByDevice"));
        assertEquals(2L, statistics.get("totalPending"));

        verify(anomalyRepository, times(1)).countByAnomalyType(any(LocalDateTime.class));
        verify(anomalyRepository, times(1)).countPendingAnomaliesByDevice();
        verify(anomalyRepository, times(1)).findByStatusOrderByDetectedAtDesc("pending");
    }
}