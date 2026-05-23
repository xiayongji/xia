package com.smarthome.analytics.service;

import com.smarthome.analytics.entity.EnergyConsumption;
import com.smarthome.analytics.repository.EnergyConsumptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnergyStatisticsServiceTest {

    @Mock
    private EnergyConsumptionRepository energyRepository;

    @InjectMocks
    private EnergyStatisticsService energyStatisticsService;

    @Test
    void testSaveEnergyData() {
        EnergyConsumption expectedConsumption = new EnergyConsumption();
        expectedConsumption.setId(1L);
        expectedConsumption.setDeviceId("device-001");
        expectedConsumption.setDeviceName("空调");
        expectedConsumption.setPower(1500.0);
        expectedConsumption.setEnergy(30.5);
        expectedConsumption.setDeviceType("空调");
        expectedConsumption.setTimestamp(LocalDateTime.now());

        when(energyRepository.save(any(EnergyConsumption.class))).thenReturn(expectedConsumption);

        EnergyConsumption result = energyStatisticsService.saveEnergyData("device-001", "空调", 1500.0, 30.5, "空调");

        assertNotNull(result);
        assertEquals("device-001", result.getDeviceId());
        assertEquals("空调", result.getDeviceName());
        assertEquals(1500.0, result.getPower());
        assertEquals(30.5, result.getEnergy());
        assertEquals("空调", result.getDeviceType());
        verify(energyRepository, times(1)).save(any(EnergyConsumption.class));
    }

    @Test
    void testGetDeviceEnergyHistory() {
        EnergyConsumption ec1 = new EnergyConsumption();
        ec1.setId(1L);
        ec1.setDeviceId("device-001");
        ec1.setDeviceName("空调");
        ec1.setPower(1500.0);
        ec1.setEnergy(10.0);
        ec1.setTimestamp(LocalDateTime.now().minusHours(2));

        EnergyConsumption ec2 = new EnergyConsumption();
        ec2.setId(2L);
        ec2.setDeviceId("device-001");
        ec2.setDeviceName("空调");
        ec2.setPower(1600.0);
        ec2.setEnergy(12.0);
        ec2.setTimestamp(LocalDateTime.now().minusHours(1));

        List<EnergyConsumption> expectedList = List.of(ec1, ec2);

        when(energyRepository.findByDeviceIdAndTimeRange(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedList);

        List<EnergyConsumption> result = energyStatisticsService.getDeviceEnergyHistory(
                "device-001", LocalDateTime.now().minusHours(3), LocalDateTime.now());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("device-001", result.get(0).getDeviceId());
        verify(energyRepository, times(1)).findByDeviceIdAndTimeRange(anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetTodayEnergyStatistics() {
        when(energyRepository.sumTotalEnergy(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(100.0);
        when(energyRepository.sumEnergyByDevice(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(new ArrayList<>());

        Map<String, Object> statistics = energyStatisticsService.getTodayEnergyStatistics();

        assertNotNull(statistics);
        assertEquals(100.0, statistics.get("totalEnergy"));
        assertEquals(100.0, statistics.get("energy"));
        assertEquals(50.0, statistics.get("totalCost"));
        assertEquals(50.0, statistics.get("cost"));
        assertEquals(100.0, statistics.get("avgDailyEnergy"));
        assertEquals(100.0, statistics.get("average"));
        assertEquals(Math.max(0, 15.0 - 100.0 * 0.1), statistics.get("savedEnergy"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> deviceEnergyList = (List<Map<String, Object>>) statistics.get("deviceEnergyList");
        assertNotNull(deviceEnergyList);
        assertTrue(deviceEnergyList.isEmpty());

        verify(energyRepository, times(1)).sumTotalEnergy(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(energyRepository, times(1)).sumEnergyByDevice(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetEnergyStatistics() {
        when(energyRepository.sumTotalEnergy(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(200.0);

        List<Object[]> deviceStats = new ArrayList<>();
        deviceStats.add(new Object[]{"device-001", 80.0});
        deviceStats.add(new Object[]{"device-002", 120.0});
        when(energyRepository.sumEnergyByDevice(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(deviceStats);

        List<Object[]> typeStats = new ArrayList<>();
        typeStats.add(new Object[]{"空调", 150.0});
        typeStats.add(new Object[]{"照明", 50.0});
        when(energyRepository.sumEnergyByDeviceType(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(typeStats);

        Map<String, Object> statistics = energyStatisticsService.getEnergyStatistics(
                LocalDateTime.now().minusDays(7), LocalDateTime.now());

        assertNotNull(statistics);
        assertEquals(200.0, statistics.get("totalEnergy"));
        assertNotNull(statistics.get("deviceStats"));
        assertNotNull(statistics.get("typeStats"));

        verify(energyRepository, times(1)).sumTotalEnergy(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(energyRepository, times(1)).sumEnergyByDevice(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(energyRepository, times(1)).sumEnergyByDeviceType(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetDeviceAveragePower() {
        when(energyRepository.avgPowerByDevice(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(50.0);

        Double result = energyStatisticsService.getDeviceAveragePower(
                "device-001", LocalDateTime.now().minusHours(1), LocalDateTime.now());

        assertNotNull(result);
        assertEquals(50.0, result);
        verify(energyRepository, times(1)).avgPowerByDevice(anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetEnergyTrend() {
        when(energyRepository.sumTotalEnergy(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(100.0, 80.0, 90.0, 110.0, 70.0, 95.0, 85.0);

        List<Map<String, Object>> trend = energyStatisticsService.getEnergyTrend(7);

        assertNotNull(trend);
        assertEquals(7, trend.size());
        for (int i = 0; i < 7; i++) {
            Map<String, Object> dayStats = trend.get(i);
            assertNotNull(dayStats.get("date"));
            assertNotNull(dayStats.get("energy"));
        }
        verify(energyRepository, times(7)).sumTotalEnergy(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetHighEnergyDevices() {
        List<Object[]> deviceStats = new ArrayList<>();
        deviceStats.add(new Object[]{"device-001", 150.0});
        deviceStats.add(new Object[]{"device-002", 120.0});
        deviceStats.add(new Object[]{"device-003", 90.0});
        deviceStats.add(new Object[]{"device-004", 60.0});
        deviceStats.add(new Object[]{"device-005", 30.0});

        when(energyRepository.sumEnergyByDevice(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(deviceStats);

        List<Map<String, Object>> result = energyStatisticsService.getHighEnergyDevices(3);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("device-001", result.get(0).get("deviceId"));
        assertEquals(150.0, result.get(0).get("totalEnergy"));
        assertEquals("device-002", result.get(1).get("deviceId"));
        assertEquals("device-003", result.get(2).get("deviceId"));

        verify(energyRepository, times(1)).sumEnergyByDevice(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetTodayEnergyStatistics_WithNullTotalEnergy() {
        when(energyRepository.sumTotalEnergy(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(null);
        when(energyRepository.sumEnergyByDevice(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(new ArrayList<>());

        Map<String, Object> statistics = energyStatisticsService.getTodayEnergyStatistics();

        assertNotNull(statistics);
        assertEquals(0.0, statistics.get("totalEnergy"));
        assertEquals(0.0, statistics.get("energy"));
        assertEquals(0.0, statistics.get("totalCost"));
        assertEquals(0.0, statistics.get("avgDailyEnergy"));
    }
}