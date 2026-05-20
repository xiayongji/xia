package com.smarthome.analytics.service;

import com.smarthome.analytics.entity.EnergyConsumption;
import com.smarthome.analytics.repository.EnergyConsumptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyStatisticsService {

    private final EnergyConsumptionRepository energyRepository;

    /**
     * 保存能耗数据
     */
    @Transactional
    public EnergyConsumption saveEnergyData(String deviceId, String deviceName, 
                                          Double power, Double energy, String deviceType) {
        EnergyConsumption consumption = new EnergyConsumption();
        consumption.setDeviceId(deviceId);
        consumption.setDeviceName(deviceName);
        consumption.setPower(power);
        consumption.setEnergy(energy);
        consumption.setDeviceType(deviceType);
        consumption.setTimestamp(LocalDateTime.now());
        
        return energyRepository.save(consumption);
    }

    /**
     * 获取设备能耗历史
     */
    public List<EnergyConsumption> getDeviceEnergyHistory(String deviceId, 
                                                          LocalDateTime startTime, 
                                                          LocalDateTime endTime) {
        return energyRepository.findByDeviceIdAndTimeRange(deviceId, startTime, endTime);
    }

    /**
     * 获取今日能耗统计
     */
    public Map<String, Object> getTodayEnergyStatistics() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        Map<String, Object> statistics = new HashMap<>();
        
        Double totalEnergy = energyRepository.sumTotalEnergy(startOfDay, endOfDay);
        double energy = totalEnergy != null ? totalEnergy : 0.0;
        statistics.put("totalEnergy", energy);
        statistics.put("energy", energy);
        statistics.put("totalCost", energy * 0.5);
        statistics.put("cost", energy * 0.5);
        statistics.put("avgDailyEnergy", energy);
        statistics.put("average", energy);
        statistics.put("savedEnergy", Math.max(0, 15.0 - energy * 0.1));

        List<Object[]> deviceStats = energyRepository.sumEnergyByDevice(startOfDay, endOfDay);
        List<Map<String, Object>> deviceEnergyList = new ArrayList<>();
        for (Object[] row : deviceStats) {
            Map<String, Object> deviceStat = new HashMap<>();
            deviceStat.put("deviceId", row[0]);
            deviceStat.put("energy", row[1]);
            deviceEnergyList.add(deviceStat);
        }
        statistics.put("deviceEnergyList", deviceEnergyList);
        
        return statistics;
    }

    /**
     * 获取指定时间段能耗统计
     */
    public Map<String, Object> getEnergyStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();
        
        Double totalEnergy = energyRepository.sumTotalEnergy(startTime, endTime);
        statistics.put("totalEnergy", totalEnergy != null ? totalEnergy : 0.0);
        
        List<Object[]> deviceStats = energyRepository.sumEnergyByDevice(startTime, endTime);
        statistics.put("deviceStats", deviceStats);
        
        List<Object[]> typeStats = energyRepository.sumEnergyByDeviceType(startTime, endTime);
        statistics.put("typeStats", typeStats);
        
        return statistics;
    }

    /**
     * 获取设备平均功率
     */
    public Double getDeviceAveragePower(String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return energyRepository.avgPowerByDevice(deviceId, startTime, endTime);
    }

    /**
     * 获取能耗趋势数据
     */
    public List<Map<String, Object>> getEnergyTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            
            Double dailyEnergy = energyRepository.sumTotalEnergy(startOfDay, endOfDay);
            
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", date.toString());
            dayStats.put("energy", dailyEnergy != null ? dailyEnergy : 0.0);
            trend.add(dayStats);
        }
        
        return trend;
    }

    /**
     * 获取高能耗设备排行
     */
    public List<Map<String, Object>> getHighEnergyDevices(int limit) {
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();
        
        List<Object[]> deviceStats = energyRepository.sumEnergyByDevice(lastWeek, now);
        
        List<Map<String, Object>> result = new ArrayList<>();
        int count = 0;
        for (Object[] row : deviceStats) {
            if (count >= limit) break;
            
            Map<String, Object> stat = new HashMap<>();
            stat.put("deviceId", row[0]);
            stat.put("deviceName", row[0]);
            stat.put("name", row[0]);
            stat.put("totalEnergy", row[1]);
            stat.put("energy", row[1]);
            result.add(stat);
            count++;
        }
        
        return result;
    }

    /**
     * 定时任务：计算每日能耗汇总
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateDailyEnergySummary() {
        log.info("开始计算每日能耗汇总");
        // 可以扩展实现每日汇总功能
    }
}